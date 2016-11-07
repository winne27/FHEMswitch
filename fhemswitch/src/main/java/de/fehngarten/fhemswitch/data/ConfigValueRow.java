package de.fehngarten.fhemswitch.data;

public class ConfigValueRow implements java.io.Serializable
{
   private static final long serialVersionUID = 1L;
   public String unit;
   public String name;
   public String value;
   public Boolean enabled;

   public ConfigValueRow(String unit, String name, String value, Boolean enabled)
   {
      this.unit = unit;
      this.name = name;
      this.value = value;
      this.enabled = enabled;
   }
}