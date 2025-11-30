package org.drk.ProyectoHibernate.user;

import org.drk.ProyectoHibernate.utils.Repository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class UserRepository implements Repository<User> {

    private final SessionFactory sessionFactory;

    public UserRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public User save(User entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(entity);
            session.getTransaction().commit();
            return entity;
        }
    }

    @Override
    public Optional<User> delete(User entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            User managed = session.merge(entity);
            session.remove(managed);
            session.getTransaction().commit();
            return Optional.of(entity);
        }
    }

    @Override
    public Optional<User> deleteById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            User entity = session.find(User.class, id.intValue());
            if (entity != null) {
                session.remove(entity);
                session.getTransaction().commit();
                return Optional.of(entity);
            } else {
                session.getTransaction().rollback();
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.find(User.class, id.intValue()));
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from User", User.class).list();
        }
    }

    @Override
    public Long count() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "select count(u) from User u", Long.class
            ).getSingleResult();
        }
    }

    public Optional<User> findByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> q = session.createQuery(
                    "from User where email = :email", User.class);
            q.setParameter("email", email);
            return Optional.ofNullable(q.uniqueResult());
        }
    }
}
