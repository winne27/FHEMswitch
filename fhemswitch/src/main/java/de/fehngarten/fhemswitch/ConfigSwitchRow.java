package de.fehngarten.fhemswitch;

class ConfigSwitchRow implements java.io.Serializable
{
   private static final long serialVersionUID = 1L;
   public String unit;
   public String name;
   public Boolean enabled;
   String cmd;

   ConfigSwitchRow(String unit, String name, Boolean enabled, String cmd)
   {
      this.unit = unit;
      this.name = name;
      this.enabled = enabled;
      this.cmd = cmd;
   }
}
