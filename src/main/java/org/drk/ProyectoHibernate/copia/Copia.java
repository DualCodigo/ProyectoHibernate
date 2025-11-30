package org.drk.ProyectoHibernate.copia;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.drk.ProyectoHibernate.pelicula.Pelicula;
import org.drk.ProyectoHibernate.user.User;

import java.io.Serializable;

@Entity
@Table(name = "copia")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Copia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_pelicula")
    private Pelicula movie;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private User user;

    private String estado;
    private String soporte;

    @Override
    public String toString() {
        return "Copia{" +
                "id=" + id +
                ", movie=" + (movie != null ? movie.getTitulo() : "null") +
                ", user=" + (user != null ? user.getEmail() : "null") +
                ", estado='" + estado + '\'' +
                ", soporte='" + soporte + '\'' +
                '}';
    }
}
