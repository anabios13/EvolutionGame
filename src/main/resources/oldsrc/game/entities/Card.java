package game.entities;

import com.google.gson.annotations.Expose;
import game.constants.Property;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Card{
    @Expose
    @Column(updatable = false,nullable = false)
    @Enumerated(EnumType.STRING)
    private Property property;

    @Expose
    @Column(updatable = false)
    @Enumerated(EnumType.STRING)
    private Property extraProperty; //use in Game.json

    @Id
    @Expose
    @Column(updatable = false)
    private int id;

    public Card(){}

    public Card(int id,Property property,Property extraProperty){
        this.id=id;
        this.property=property;
        this.extraProperty=extraProperty;
    }

    public Card(int id,Property property){
        this.id=id;
        this.property=property;
    }

    public int getId(){
        return id;
    }

//    @Override
//    public boolean equals(Object o){
//        if (!(o instanceof Card)) return false;
//        Card card=(Card)o;
//        return card.id==this.id;
//    }
//
//    @Override
//    public int hashCode(){
//        return Objects.hash(id,property,extraProperty);
//    }

//    public String getFullJson() {
//        JsonObjectBuilder builder = JsonProvider.provider().createObjectBuilder();
//        builder.add("id", id)
//                .add("property", property);
//        if (extraProperty != null)
//            builder.add("extraProperty", extraProperty);
//
//        return builder.build().toString();
//    }


}

