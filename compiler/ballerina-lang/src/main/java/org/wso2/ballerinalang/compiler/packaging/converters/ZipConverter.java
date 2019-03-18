package org.wso2.ballerinalang.compiler.packaging.converters;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipError;

/**
 * Provide functions to convert a patten to a stream of zip paths.
 */
public class ZipConverter extends PathConverter {

    public ZipConverter(Path archivePath) {
        super(resolveIntoArchive(archivePath));
    }

    @Override
    public Path combine(Path path, String pathPart) {
        return resolveIntoArchive(path.resolve(pathPart));
    }

    private static Path resolveIntoArchive(Path newPath) {
        String pathPart = newPath.toString();
        if ((pathPart.endsWith(".zip") || pathPart.endsWith(".jar")) && Files.isRegularFile(newPath)) {
            return pathWithinZip(newPath.toUri());
        } else {
            return newPath;
        }
    }

    private static Path pathWithinZip(URI pathToZip) {
        try {
            URI pathInZip = new URI("jar:" + pathToZip.getScheme(),
                    pathToZip.getUserInfo(), pathToZip.getHost(), pathToZip.getPort(),
                    pathToZip.getPath() + "!/",
                    pathToZip.getQuery(), pathToZip.getFragment());
            initFS(pathInZip);
            return Paths.get(pathInZip);
        } catch (URISyntaxException ignore) {
            // This exception occurs when trying to read the balo which is inside the package zip. An exception can
            // occur when creating the URI needed to create the zip file system provider which will be used to read the
            // content inside the zip/jar file. So if such an error occurs, we just return the path to the zip/jar file
            // instead of throwing the exception.
        }
        return Paths.get(pathToZip);
    }

    private static void initFS(URI uri) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try {
            FileSystems.newFileSystem(uri, env);
        } catch (FileSystemAlreadyExistsException ignore) {
            // A file system will be always created when we are accessing zip/jar when resolving dependencies. So when
            // we are accessing the same zip/jar for the second time sometimes that filesystem might already exist.
            // Since we have no way to check if a filesystem is already created for a particular zip/jar, we have
            // ignored this exception.
        } catch (IOException | ZipError e) {
            throw new BLangCompilerException("Error loading balo " + uri.getPath(), e);
        }
    }

    @Override
    public Stream<Path> getLatestVersion(Path path, PackageID packageID) {
        if (!Files.isDirectory(path)) {
            return Stream.of();
        }
        try {
            List<Path> pathList = new ArrayList<>();
            if (packageID != null) {
                String pkgName = packageID.getName().getValue();
                pathList = Files.list(path)
                                .map(SortablePath::new)
                                .filter(SortablePath::valid)
                                .filter(sortablePath -> validBaloPath(pkgName, sortablePath))
                                .sorted(Comparator.reverseOrder())
                                .limit(1)
                                .map(SortablePath::getPath)
                                .collect(Collectors.toList());
                if (packageID.version.value.isEmpty() && !packageID.orgName.equals(Names.BUILTIN_ORG)
                        && !packageID.orgName.equals(Names.ANON_ORG) && pathList.size() > 0) {
                    Path modulePath = pathList.get(0);
                    packageID.version = new Name(modulePath.toFile().getName());

                    // Check for cache invalidity when resolving dependencies using nightly builds
                    if (RepoUtils.isANightlyBuild() && checkForCacheInvalidity(pkgName, modulePath)) {
                        packageID.version = Names.EMPTY;
                        return Stream.of();
                    }
                }
            }
            return pathList.stream();
        } catch (IOException ignore) {
            // An I/O exception occurs when opening the directory specified when listing the files inside it. Since
            // this is done during dependency resolution, we don't throw an exception to the user so instead we
            // return an empty stream.
        }
        return Stream.of();
    }

    /**
     * Validates the package with compiler version.
     *
     * @param pkgName      package name
     * @param sortablePath sortable path
     * @return if the package is compatible with the compiler version
     */
    private boolean validBaloPath(String pkgName, SortablePath sortablePath) {
        Path zipPath = resolveIntoArchive(sortablePath.getPath()
                                                      .resolve(pkgName + ProjectDirConstants.BLANG_COMPILED_PKG_EXT));
        return filterByBaloVersion(zipPath.resolve(ProjectDirConstants.USER_REPO_OBJ_DIRNAME)
                                          .resolve(pkgName + ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT));
    }

    /**
     * Filter by balo version.
     *
     * @param path package path
     * @return if the balo version of the package is compatible with compiler version
     */
    private boolean filterByBaloVersion(Path path) {
        try (InputStream stream = Files.newInputStream(path)) {
            byte[] data = new byte[ProgramFileConstants.VERSION_BYTE];
            if (stream.read(data) != -1) {
                short version = data[data.length - 1];
                return (version >= ProgramFileConstants.MIN_SUPPORTED_VERSION) &&
                        (version <= ProgramFileConstants.MAX_SUPPORTED_VERSION);
            }
        } catch (IOException ignore) {
            // An I/O exception can occur when opening the balo file which will return an input stream to read the balo
            // version. Since this is done during dependency resolution, we don't throw an exception instead we return
            // false
        }
        return false;
    }

    /**
     * Check if the module in caches in invalid.
     *
     * @param pkgName    package name
     * @param modulePath module path
     * @return if the module cached is invalid or not
     */
    private boolean checkForCacheInvalidity(String pkgName, Path modulePath) {
        // Check if the cached module was pulled from a nightly, if not return false
        if (!Files.exists(modulePath.resolve(ProjectDirConstants.NIGHTLY_BUILD))) {
            return false;
        }
        // Get the module.zip path
        Path moduleZipPath = modulePath.resolve(pkgName + ProjectDirConstants.BLANG_COMPILED_PKG_EXT);

        // Get modified of the module zip file.
        Date modifiedDate = new Date(moduleZipPath.toFile().lastModified());
        // Set the cache invalidation time as the midnight of today
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date midnightDate = cal.getTime();

        // If the module was not pulled before the cache invalidation time and if it was pulled on the same day, return
        // false
        if (!modifiedDate.before(midnightDate)) {
            return false;
        }
        // The module cached has been invalidated so clean the directory and pull again.
        try {
            //Delete the metadata file
            Files.deleteIfExists(modulePath.resolve(ProjectDirConstants.NIGHTLY_BUILD));
            // Delete the module.zip
            Files.deleteIfExists(moduleZipPath);
            // Delete all the empty directories
            deleteEmptyParentDirs(modulePath, this.getRoot());
        } catch (IOException ignore) {
            // An I/O exception occurs when deleting the files inside the downloaded mod. Since
            // this is done during dependency resolution, we don't throw an exception to the user so instead we
            // return an empty stream.
        }
        return true;
    }

    /**
     * Delete empty parent directories.
     * After deleting the module.zip from the home repository we should delete the empty directories as well. We need
     * to delete all folders from the module path to the home directory only if they are empty (going backwards).
     * If the directory path is empty i.e. does not contain any files we simply delete the folder else we don't delete.
     *
     * @param modulePath package directory path
     * @param repoPath   home repository path
     * @throws IOException throw an exception if an error occurs
     */
    private static void deleteEmptyParentDirs(Path modulePath, Path repoPath) throws IOException {
        Path pathsInBetween = repoPath.relativize(modulePath);
        for (int i = pathsInBetween.getNameCount(); i > 0; i--) {
            Path toRemove = repoPath.resolve(pathsInBetween.subpath(0, i));
            if (!Files.list(toRemove).findAny().isPresent()) {
                Files.delete(toRemove);
            }
        }
    }
}
