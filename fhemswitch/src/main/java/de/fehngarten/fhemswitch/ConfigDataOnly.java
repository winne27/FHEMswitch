package de.fehngarten.fhemswitch;

import java.util.ArrayList;

class ConfigDataOnly implements java.io.Serializable
{
   private static final long serialVersionUID = 1L;
   String urljs = "https://your-domain.tld:8086";
   String urlpl = "https://your-domain.tld:8082/fhem";
   ArrayList<ConfigSwitchRow> switchRows = new ArrayList<>();
   ArrayList<ConfigLightsceneRow> lightsceneRows = new ArrayList<>();
   ArrayList<ConfigValueRow> valueRows = new ArrayList<>();
   ArrayList<ConfigCommandRow> commandRows = new ArrayList<>();
   String connectionPW = "";
   int layoutLandscape;
   int layoutPortrait;
   int switchCols;
   int valueCols;
   int commandCols;

   ConfigDataOnly()
   {
      this.layoutPortrait = 1; 
      this.layoutLandscape = 0;
      this.switchCols = 0;
      this.valueCols = 0;
      this.commandCols = 0;
   }
}
