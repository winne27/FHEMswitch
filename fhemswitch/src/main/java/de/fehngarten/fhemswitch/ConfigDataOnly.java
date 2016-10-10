package de.fehngarten.fhemswitch;

import java.util.ArrayList;

public class ConfigDataOnly implements java.io.Serializable
{
   private static final long serialVersionUID = 1L;
   public String urljs = "https://your-domain.tld:8086";
   public String urlpl = "https://your-domain.tld:8082/fhem";
   public ArrayList<ConfigSwitchRow> switchRows = new ArrayList<ConfigSwitchRow>();
   public ArrayList<ConfigLightsceneRow> lightsceneRows = new ArrayList<ConfigLightsceneRow>();
   public ArrayList<ConfigValueRow> valueRows = new ArrayList<ConfigValueRow>();
   public ArrayList<ConfigCommandRow> commandRows = new ArrayList<ConfigCommandRow>();
   public String connectionPW = "";
   public int layoutLandscape;
   public int layoutPortrait;
   public int switchCols;
   public int valueCols;
   public int commandCols;

   public ConfigDataOnly()
   {
      this.layoutPortrait = 1; 
      this.layoutLandscape = 0;
      this.switchCols = 0;
      this.valueCols = 0;
      this.commandCols = 0;
   }
}
