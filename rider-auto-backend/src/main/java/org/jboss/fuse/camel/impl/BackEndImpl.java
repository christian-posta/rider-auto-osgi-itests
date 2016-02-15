package org.jboss.fuse.camel.impl;

import org.jboss.fuse.camel.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackEndImpl {
    private static final Logger LOG = LoggerFactory.getLogger(BackEndImpl.class);
    
    public void doWork(Order order) {
        LOG.info("Received order for {} {}s.", order.getAmount(), order.getName());
    }
}
