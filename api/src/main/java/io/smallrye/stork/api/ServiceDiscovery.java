package io.smallrye.stork.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.smallrye.mutiny.Uni;

/**
 * Interface to retrieve the list of all available service instances for a given service.
 */
public interface ServiceDiscovery {
    /**
     * Retrieves the service instances.
     * <p>
     * This retrieval is an asynchronous action, thus, the method returns a {@link Uni}
     *
     * @return all `ServiceInstance`'s for the service
     */
    Uni<List<ServiceInstance>> getServiceInstances();

    /**
     * Optional initialization.
     * This method will be invoked after all service discoveries and load balancers are registered in Stork
     *
     * @param stork the stork instance managing the service.
     */
    default void initialize(StorkServiceRegistry stork) {
    }

    default Uni<List<ServiceInstance>> getServiceInstances(String... labels) {
        return getServiceInstances();
    };

    default List<ServiceInstance> filterByLabel(List<ServiceInstance> instances, String... labels) {
        if (instances == null || instances.isEmpty() || labels == null || labels.length == 0) {
            return Collections.emptyList();
        }

        Set<String> labelSet = Arrays.stream(labels)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        return instances.stream()
                .filter(instance -> {
                    Map<String, String> metadata = (Map<String, String>) instance.getMetadata();
                    if (metadata == null || metadata.isEmpty()) {
                        return false;
                    }
                    // Devuelve true si alguna label coincide con alguna key o value del metadata
                    return metadata.entrySet().stream()
                            .anyMatch(entry -> labelSet.contains(entry.getKey()) ||
                                    labelSet.contains(entry.getValue()));
                })
                .collect(Collectors.toList());

    }
}
