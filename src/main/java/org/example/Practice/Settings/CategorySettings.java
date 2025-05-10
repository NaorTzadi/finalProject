package org.example.Practice.Settings;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class CategorySettings {
    //toNote: should have added solution detail to the category
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) private Options.MathCategory mathCategory;
    protected CategorySettings() {}

    protected CategorySettings(Options.MathCategory mathCategory) {this.mathCategory = mathCategory;}
    public Long getId() {return id;}
    public Options.MathCategory getMathCategory() {return mathCategory;}
    public void setId(Long id) {this.id = id;}
    public void setMathCategory(Options.MathCategory mathCategory) {this.mathCategory = mathCategory;}

    public String toJSON() {
        if (this instanceof GeometrySettings geometrySettings){
            return geometrySettings.toJSON();
        }else if (this instanceof ArithmeticSettings arithmeticSettings){
            return arithmeticSettings.toJSON();
        }
        return null;
    }

    @Override
    public String toString() {
        return "CategorySettings{" +
                "id=" + id +
                ", mathCategory=" + mathCategory +
                '}';
    }
}