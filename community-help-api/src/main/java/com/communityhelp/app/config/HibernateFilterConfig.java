package com.communityhelp.app.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class HibernateFilterConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Activa el filtro "activeFilter" para todas las consultas de User por defecto.
     */
    @PostConstruct
    @Transactional
    public void enableActiveFilter() {
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("activeFilter");
        filter.validate();
    }
}