/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.wso2.ballerina.containers.docker.impl;

import io.fabric8.docker.api.model.Image;
import io.fabric8.docker.api.model.ImageDelete;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.ballerina.containers.Constants;
import org.wso2.ballerina.containers.docker.BallerinaDockerClient;
import org.wso2.ballerina.containers.docker.exception.BallerinaDockerClientException;
import org.wso2.ballerina.containers.docker.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

/**
 * Default implementation of the {@link BallerinaDockerClient}.
 */
public class DefaultBallerinaDockerClient implements BallerinaDockerClient {

    private static final String PATH_FILES = "files";
    private static final String PATH_DOCKER_IMAGE_ROOT = "docker/image";
    private static final String PATH_DOCKERFILE_NAME = "Dockerfile";
    private static final String PATH_TEMP_DOCKERFILE_CONTEXT_PREFIX = "ballerina-docker-";
    private static final String PATH_BAL_FILE_EXT = ".bal";
    private static final String ENV_SVC_MODE = "SVC_MODE";
    private static final String ENV_FILE_MODE = "FILE_MODE";
    private static final String LOCAL_DOCKER_DAEMON_SOCKET = "unix:///var/run/docker.sock";

    private final CountDownLatch buildDone = new CountDownLatch(1);
    // Cannot depend on buildErrors because Fabric8 seems to be randomly adding "Failed:" errors even
    // when the build completed successfully.
    private final List<String> buildErrors = new ArrayList<>();

    @Override
    public String createServiceImage(String packageName, String dockerEnv, Path bPackagePath,
                                     String imageName, String imageVersion)
            throws BallerinaDockerClientException, IOException, InterruptedException {

        return createImageFromPackage(packageName, dockerEnv, bPackagePath, true, imageName, imageVersion);
    }

    @Override
    public String createServiceImage(String serviceName, String dockerEnv, String ballerinaConfig,
                                     String imageName, String imageVersion)
            throws InterruptedException, BallerinaDockerClientException, IOException {

        return createImageFromSingleConfig(serviceName, dockerEnv, ballerinaConfig, true,
                imageName, imageVersion);
    }

    @Override
    public String createMainImage(String packageName, String dockerEnv, Path bPackagePath,
                                  String imageName, String imageVersion)
            throws BallerinaDockerClientException, IOException, InterruptedException {

        return createImageFromPackage(packageName, dockerEnv, bPackagePath, false, imageName, imageVersion);
    }

    @Override
    public String createMainImage(String mainPackageName, String dockerEnv, String ballerinaConfig,
                                  String imageName, String imageVersion)
            throws InterruptedException, BallerinaDockerClientException, IOException {

        return createImageFromSingleConfig(mainPackageName, dockerEnv, ballerinaConfig, false,
                imageName, imageVersion);
    }

    @Override
    public boolean deleteImage(String packageName, String dockerEnv, String imageName, String imageVersion)
            throws BallerinaDockerClientException {

        // TODO: should not be able to delete arbitrary images.
        imageName = getImageName(packageName, imageName, imageVersion);
        List<ImageDelete> imageDeleteList;

        try {
            imageDeleteList = getDockerClient(dockerEnv).image()
                    .withName(imageName)
                    .delete()
                    .force()
                    .andPrune(false);

        } catch (DockerClientException e) {
            if (e.getMessage().contains("No such image")) {
                return false;
            }
            throw e;
        }

        return imageDeleteList.size() != 0;
    }

    @Override
    public String getImage(String imageName, String dockerEnv) {
        DockerClient client = getDockerClient(dockerEnv);
        List<Image> images = client.image().list().filter(imageName).endImages();
        for (Image image : images) {
            String currentImageName = image.getRepoTags().get(0);
            if (currentImageName.equals(imageName + ":" + Constants.IMAGE_VERSION_LATEST) ||
                    currentImageName.equals(imageName)) {
                return currentImageName;
            }
        }

        return null;
    }

    private String createImageFromPackage(String packageName, String dockerEnv, Path bPackagePath, boolean isService,
                                          String imageName, String imageVersion)
            throws BallerinaDockerClientException, IOException, InterruptedException {

        if (bPackagePath == null) {
            throw new BallerinaDockerClientException("Invalid Ballerina package");
        }

        if (!Files.exists(bPackagePath)) {
            throw new BallerinaDockerClientException("Cannot find Ballerina Package file: " + bPackagePath.toString());
        }

        imageName = getImageName(packageName, imageName, imageVersion);

        // 1. Create a tmp docker context
        Path tmpDir = prepTempDockerfileContext();

        // 2. Copy Ballerina package
        Files.copy(
                bPackagePath,
                Paths.get(tmpDir.toString() + File.separator + PATH_FILES + File.separator
                        + bPackagePath.toFile().getName()),
                StandardCopyOption.REPLACE_EXISTING);

        // 3. Create a docker image from the temp context
        String buildArgs = "{\"" + ENV_SVC_MODE + "\":\"" + String.valueOf(isService) + "\"}";
        buildImage(dockerEnv, imageName, tmpDir, buildArgs);

        // 4. Cleanup
        cleanupTempDockerfileContext(tmpDir);

        return getImage(imageName, dockerEnv);
    }

    private String createImageFromSingleConfig(String serviceName, String dockerEnv, String ballerinaConfig,
                                               boolean isService, String imageName, String imageVersion)
            throws BallerinaDockerClientException, IOException, InterruptedException {

        imageName = getImageName(serviceName, imageName, imageVersion);

        // 1. Create a tmp docker context
        Path tmpDir = prepTempDockerfileContext();

        // 2. Create a .bal file inside context/files
        Path ballerinaFile = Files.createFile(Paths.get(tmpDir + File.separator + PATH_FILES + File.separator +
                serviceName + PATH_BAL_FILE_EXT));

        Files.write(ballerinaFile, ballerinaConfig.getBytes(Charset.defaultCharset()),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // 3. Create a docker image from the temp context
        String buildArgs = "{\"" + ENV_SVC_MODE + "\":\"" + String.valueOf(isService) + "\", " +
                "\"" + ENV_FILE_MODE + "\":\"true\"}";
        buildImage(dockerEnv, imageName, tmpDir, buildArgs);

        // 4. Cleanup
        cleanupTempDockerfileContext(tmpDir);

        return getImage(imageName, dockerEnv);
    }

    /**
     * Generate the image name from the given parameters.
     *
     * @param packageName  The Ballerina Package name.
     * @param imageName    The given image name. This can be null, in which case the package name is used as the
     *                     image name.
     * @param imageVersion The given image version. If the imageName is not null, this should not be null. If imageName
     *                     is null, this value is ignored and "latest" is used as the image version.
     * @return The image name derived from the above information.
     * @throws BallerinaDockerClientException If any of the required parameters are not found.
     */
    private String getImageName(String packageName, String imageName, String imageVersion)
            throws BallerinaDockerClientException {

        if (StringUtils.isEmpty(packageName)) {
            throw new BallerinaDockerClientException("Package name should not be null or empty.");
        }

        if (imageName == null) {
            imageName = packageName.toLowerCase(Locale.getDefault()) + ":" + Constants.IMAGE_VERSION_LATEST;
        } else {
            if (imageVersion == null) {
                throw new BallerinaDockerClientException("Image version cannot be null when Image name is specified.");
            }

            imageName = imageName.toLowerCase(Locale.getDefault()) + ":" + imageVersion;
        }
        return imageName;
    }

    /**
     * Creates a {@link DockerClient} from the given Docker host URL.
     *
     * @param env The URL of the Docker host. If this is null, a {@link DockerClient} pointed to the local Docker
     *            daemon will be created.
     * @return {@link DockerClient} object.
     */
    private DockerClient getDockerClient(String env) {
        DockerClient client;
        if (env == null) {
            env = LOCAL_DOCKER_DAEMON_SOCKET;
        }

        Config dockerClientConfig = new ConfigBuilder()
                .withDockerUrl(env)
                .build();

        client = new io.fabric8.docker.client.DefaultDockerClient(dockerClientConfig);
        return client;
    }

    private void cleanupTempDockerfileContext(Path tmpDir) throws IOException {
        FileUtils.deleteDirectory(tmpDir.toFile());
    }

    private Path prepTempDockerfileContext() throws IOException {
        String tempDirName = PATH_TEMP_DOCKERFILE_CONTEXT_PREFIX + String.valueOf(Instant.now().getEpochSecond());
        Path tmpDir = Files.createTempDirectory(tempDirName);
        Files.createDirectory(Paths.get(tmpDir.toString() + File.separator + PATH_FILES));
        Files.copy(
                Paths.get(Utils.getResourceFile(PATH_DOCKER_IMAGE_ROOT + File.separator +
                        PATH_DOCKERFILE_NAME).getAbsolutePath()),
                Paths.get(tmpDir.toString() + File.separator + PATH_DOCKERFILE_NAME),
                StandardCopyOption.REPLACE_EXISTING);

        return tmpDir;
    }

    private void buildImage(String dockerEnv, String imageName, Path tmpDir, String buildArgs)
            throws InterruptedException, IOException {

        DockerClient client = getDockerClient(dockerEnv);
        OutputHandle buildHandle = client.image()
                .build()
                .withRepositoryName(imageName)
                .withNoCache()
                .alwaysRemovingIntermediate()
                .withBuildArgs(buildArgs)
                .usingListener(new DockerBuilderEventListener())
                .writingOutput(System.out)
                .fromFolder(tmpDir.toString());

        buildDone.await();
        buildHandle.close();
        client.close();
    }

    /**
     * An {@link EventListener} implementation to listen to Docker build events.
     */
    private class DockerBuilderEventListener implements EventListener {
        @Override
        public void onSuccess(String successEvent) {
            buildDone.countDown();
        }

        @Override
        public void onError(String errorEvent) {
            buildErrors.add(errorEvent);
            buildDone.countDown();
        }

        @Override
        public void onEvent(String ignore) {
            //..
        }
    }

//    private static boolean isFunctionImage(DockerClient client, String serviceName) {
//        for (String envVar : client.image()
//                .withName(serviceName.toLowerCase(Locale.getDefault()) + ":latest")
//                .inspect()
//                .getConfig()
//                .getEnv()) {
//
//            String[] envVarValue = envVar.split("=");
//            if (envVarValue[0].equals("SVC_MODE") && envVarValue[1].equals("false")) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    @Override
//    public String runMainContainer(String dockerEnv, String serviceName)
//            throws InterruptedException, IOException, BallerinaDockerClientException {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        DockerClient client = getDockerClient(dockerEnv);
//        if (!isFunctionImage(client, serviceName)) {
//            throw new BallerinaDockerClientException("Invalid image to run: " +
// serviceName.toLowerCase(Locale.getDefault()) +
//                    ":latest");
//        }
//
//        ContainerCreateResponse container = client.container().createNew()
//                .withName(serviceName + "-latest")
//                .withImage(serviceName.toLowerCase(Locale.getDefault()) + ":latest")
//                .done();
//
//        // TODO: throws EOFException here.
//        try (
//                OutputHandle logHandle = client.container().
//                        withName(container.getId())
//                        .logs()
//                        .writingOutput(outputStream)
//                        .writingError(outputStream)
//                        .display()
//        ) {
//
//            if (client.container().withName(container.getId()).start()) {
//////                ("Container started: " + container.getId());
//                Thread.sleep(10000);
////                client.container().withName(container.getId()).stop();
////                return IOUtils.toString(logHandle.getOutput(), "UTF-8");
//                client.container().withName(container.getId()).remove();
//                return new String(outputStream.toByteArray(), Charset.defaultCharset());
////                return "";
//            }
//        }
//
//        client.container().withName(container.getId()).remove();
//        return "";
//
//    }
//
//    @Override
//    public String runServiceContainer(String packageName, String dockerEnv) throws BallerinaDockerClientException {
//        DockerClient client = getDockerClient(dockerEnv);
//        if (isFunctionImage(client, packageName)) {
//            throw new BallerinaDockerClientException("Invalid image to run: " +
// packageName.toLowerCase(Locale.getDefault()) +
//                    ":latest");
//        }
//
//        ContainerCreateResponse container = client.container().createNew()
//                .withName(packageName + "-latest")
//                .withImage(packageName.toLowerCase(Locale.getDefault()) + ":latest")
//                .done();
//
//        client.container().withName(container.getId()).start();
////        if (client.container().withName(container.getId()).start()) {
//////            ("Container started: " + container.getId());
////        }
//
//        String dockerUrl;
//        if (dockerEnv == null) {
//            dockerUrl = "http://localhost:" + "9090" + File.separator;
//        } else {
//            dockerUrl = dockerEnv.substring(0, dockerEnv.lastIndexOf(":")) + "9090" + File.separator;
//        }
//
//        return dockerUrl;
//    }
//
//    @Override
//    public void stopContainer(String packageName, String dockerEnv) throws BallerinaDockerClientException {
////        DockerClient client = getDockerClient(dockerEnv);
//        throw new BallerinaDockerClientException("Not implemented!");
//    }
}
