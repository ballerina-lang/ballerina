package org.wso2.ballerinalang.compiler.packaging;

import org.ballerinalang.repository.CompilerInput;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;

import java.util.List;

/**
 * List of resolved sources and the RepoHierarchy used to resolve it.
 * Had to wrap in a class since you can't return multiple items form a java method.
 */
public class Resolution {
    public static final Resolution NOT_FOUND = new Resolution(null, null, null);
    public final RepoHierarchy resolvedBy;
    public final List<CompilerInput> inputs;
    public final Repo resolvedRepository;

    Resolution(RepoHierarchy resolvedBy, List<CompilerInput> inputs, Repo resolvedRepository) {
        this.resolvedBy = resolvedBy;
        this.inputs = inputs;
        this.resolvedRepository = resolvedRepository;
    }
}
