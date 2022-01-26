package de.proto4j.internal.model;//@date 23.01.2022

import de.proto4j.internal.model.bean.BeanManager;

public interface BoundConfiguration {

    String getConfigurationName();

    Package getPackage();

    BeanManager getBeanManager();

}
