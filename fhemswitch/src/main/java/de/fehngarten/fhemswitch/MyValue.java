package de.fehngarten.fhemswitch;

class MyValue  implements Comparable<MyValue>
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

   @Override
   public int compareTo(MyValue compSwitch) {
      return this.unit.compareTo(compSwitch.unit);
   }
}