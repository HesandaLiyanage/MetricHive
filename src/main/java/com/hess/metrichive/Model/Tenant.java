package com.hess.metrichive.Model;



import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "tenants")
@Data
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false , unique = true)
    public String email;

    @Column(nullable = false, unique = true , name = "api_key")
    public String apiKey;

    @Column(nullable = false , size)
    public String tier = "Free"
}
