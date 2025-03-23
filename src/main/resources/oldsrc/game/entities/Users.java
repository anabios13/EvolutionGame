package game.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
//@Table(name="Users") //by default, table name=Classname
public class Users{

    @Id //primary key
    @Column //default column name=field name
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true,nullable = false,updatable = false)
    private String login;

    @Column(length = 20,nullable = false)
    private byte[] password;

    @Column(length = 8)
    private byte[] salt;

    public Users(){}

    public Users(String login, byte[] password, byte[] salt){
        this.login=login;
        this.password=password;
        this.salt=salt;
    }

    public Users(String login){
        this.login=login;
    }

    public String getLogin(){
        return login;
    }

    public void setLogin(String login){
        this.login=login;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    //Аннотация @Column  не является обязательной. По умолчанию все поля класса сохраняются в базе данных.
    // Если поле не должно быть сохранено, оно должно быть проаннотированно аннотацией @Transient.

}
