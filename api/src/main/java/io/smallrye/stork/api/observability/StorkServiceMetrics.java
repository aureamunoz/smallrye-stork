package io.smallrye.stork.api.observability;

import java.time.Duration;
import java.util.List;

import io.smallrye.stork.api.ServiceInstance;

public class StorkServiceMetrics {

    // Metadata
    protected final String serviceName;
    protected final String serviceDiscoveryType;
    protected final String serviceSelectionType;

    // Time
    protected final long begin;
    protected volatile long endOfServiceDiscovery;
    protected volatile long endOfServiceSelection;

    // Service discovery data
    protected volatile int instancesCount = -1;

    // Service selection data
    protected volatile long selectedInstanceId = -1L;

    // Overall status
    protected volatile boolean done;
    protected volatile boolean serviceDiscoveryDone;
    protected volatile Throwable failure;

    public StorkServiceMetrics(String serviceName, String serviceDiscoveryType, String serviceSelectionType) {
        this.serviceName = serviceName;
        this.serviceDiscoveryType = serviceDiscoveryType;
        this.serviceSelectionType = serviceSelectionType;
        this.begin = System.nanoTime();
    }

    public void onServiceDiscoverySuccess(List<ServiceInstance> instances) {
        this.endOfServiceDiscovery = System.nanoTime();
        this.serviceDiscoveryDone = true;
        if (instances != null) {
            this.instancesCount = instances.size();
        } else {
            this.instancesCount = 0;
        }
    }

    public void onServiceDiscoveryFailure(Throwable throwable) {
        this.endOfServiceDiscovery = System.nanoTime();
        this.serviceDiscoveryDone = true;
        this.failure = throwable;
    }

    public void onServiceSelectionSuccess(long id) {
        this.endOfServiceSelection = System.nanoTime();
        this.selectedInstanceId = id;
        this.done = true;
    }

    public void onServiceSelectionFailure(Throwable throwable) {
        this.endOfServiceSelection = System.nanoTime();
        if (failure != throwable) { //if SD fails we don't know count the error as a LB error
            this.failure = throwable;
        }
    }

    public boolean isDone() {
        return done || failure != null;
    }

    public Duration getOverallDuration() {
        if (!isDone()) {
            return null;
        }
        return Duration.ofNanos(endOfServiceSelection - begin);
    }

    public Duration getServiceDiscoveryDuration() {
        if (!serviceDiscoveryDone) {
            return null;
        }
        return Duration.ofNanos(endOfServiceDiscovery - begin);
    }

    public Duration getServiceSelectionDuration() {
        if (!isDone()) {
            return null;
        }
        return Duration.ofNanos(endOfServiceSelection - endOfServiceDiscovery);
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceDiscoveryType() {
        return serviceDiscoveryType;
    }

    public String getServiceSelectionType() {
        return serviceSelectionType;
    }

    public int getDiscoveredInstancesCount() {
        return instancesCount;
    }

    public Throwable failure() {
        return failure;
    }

    public long getSelectedInstanceId() {
        return selectedInstanceId;
    }

}