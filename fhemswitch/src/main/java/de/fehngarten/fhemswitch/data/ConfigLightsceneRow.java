package de.fehngarten.fhemswitch.data;

public class ConfigLightsceneRow implements java.io.Serializable
{
   private static final long serialVersionUID = 1L;
   public String unit;
   public String name;
   public Boolean enabled;
   public Boolean isHeader;
   public Boolean showHeader;

   public ConfigLightsceneRow(String unit, String name, Boolean enabled, Boolean isHeader, Boolean showHeader)
   {
      this.unit = unit;
      this.name = name;
      this.enabled = enabled;
      this.isHeader = isHeader;
      this.showHeader = showHeader;
   }
}