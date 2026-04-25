/*

- records are a quicker way to create data-carrying classes in Java. 

- They automatically generate boilerplate code like constructors, getters, setters, equals(), hashCode()


*----------- structure of records in java ----------------*

  record ClassName(fieldType1 fieldName1, fieldType2 fieldName2, ...) {
      // optional: additional methods or static fields
  }

*-------------- what records is defining -----------------*

class ClassName {
    private final fieldType1 fieldName1;
    private final fieldType2 fieldName2;
    // ... other fields

    // Constructor
    public ClassName(fieldType1 fieldName1, fieldType2 fieldName2, ...) {
        this.fieldName1 = fieldName1;
        this.fieldName2 = fieldName2;
        // ... initialize other fields
    }

    // Getters
    public fieldType1 fieldName1() {
        return fieldName1;
    }

    public fieldType2 fieldName2() {
        return fieldName2;
    }

    // the functions of equals(), hashCode(), and toString() methods are also automatically generated within the class for records
}

*-------------- Nested records  -----------------*

- we can nest records or just general methods inside a record to encapsulate related data structures together.


*-------------- Nested records structures  -----------------*

*we defined one of the parameters as the nested record itself like below, we do this so we can access the nested record*

record(NestedRecord nested, fieldType1 fieldName1, fieldType2 fieldName2, ...) {
    
    record NestedRecord(nestedFieldType1 nestedFieldName1, ...) {
        // optional: additional methods or static fields
    }

    // optional: additional methods or static fields
}


*-------------- Nested records structures  -----------------*

- the reason we can use nested records is it  helps in encapsulating the data and its related structure within a single 
  parent record, which can be beneficial for maintaining a clear hierarchy and relationships 
  between different data components.


*-------------- @jsonproperties  -----------------*

- @JsonProperty annotation is used to map JSON property names to Java record fields when the names differ

syntax is :

 (@JsonProperty("json_property_name") fieldType fieldName1, ...)



*/
