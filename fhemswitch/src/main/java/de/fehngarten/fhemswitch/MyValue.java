package de.fehngarten.fhemswitch;

class MyValue
{
   public String name;
   public String unit;
   public String value;

   MyValue(String name, String unit)
   {
      this.name = name;
      this.unit = unit;
      this.value = "";
   }

   public void setValue(String value)
   {
      this.value = value;
   }


}