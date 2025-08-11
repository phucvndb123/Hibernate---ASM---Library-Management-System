package com.libsys.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String bio;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Version
    private long version;

    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();

    public Author() {
    }

    public Author(String name)
    {
        this.name = name;
    }

    public Long getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getBio() {

        return bio;
    }

    public void setBio(String bio) {

        this.bio = bio;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public long getVersion() {
        return version;
    }

    public Set<Book> getBooks() {
        return books;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Author)) return false;
        Author a = (Author) o;
        return Objects.equals(name, a.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
