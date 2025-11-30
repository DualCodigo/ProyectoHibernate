module org.drk.ProyectoHibernate {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires static lombok;
    requires java.naming;


    opens org.drk.ProyectoHibernate;
    exports org.drk.ProyectoHibernate;
    opens org.drk.ProyectoHibernate.utils;
    exports org.drk.ProyectoHibernate.utils;
    opens org.drk.ProyectoHibernate.controllers;
    exports org.drk.ProyectoHibernate.controllers;
    opens org.drk.ProyectoHibernate.user;
    exports org.drk.ProyectoHibernate.user;
    opens org.drk.ProyectoHibernate.pelicula;
    exports org.drk.ProyectoHibernate.pelicula;
    opens org.drk.ProyectoHibernate.copia;
    exports org.drk.ProyectoHibernate.copia;
}