package de.fehngarten.fhemswitch;

public class ConfigSwitchRow implements java.io.Serializable
{
   private static final long serialVersionUID = 1L;
   public String unit;
   public String name;
   public Boolean enabled;
   public String cmd;

   public ConfigSwitchRow(String unit, String name, Boolean enabled, String cmd)
   {
      this.unit = unit;
      this.name = name;
      this.enabled = enabled;
      this.cmd = cmd;
   }
}
