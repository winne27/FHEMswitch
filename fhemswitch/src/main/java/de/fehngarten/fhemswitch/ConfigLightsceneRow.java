package de.fehngarten.fhemswitch;

class ConfigLightsceneRow implements java.io.Serializable
{
   private static final long serialVersionUID = 1L;
   public String unit;
   public String name;
   Boolean enabled;
   Boolean isHeader;
   Boolean showHeader;

   ConfigLightsceneRow(String unit, String name, Boolean enabled, Boolean isHeader, Boolean showHeader)
   {
      this.unit = unit;
      this.name = name;
      this.enabled = enabled;
      this.isHeader = isHeader;
      this.showHeader = showHeader;
   }
}