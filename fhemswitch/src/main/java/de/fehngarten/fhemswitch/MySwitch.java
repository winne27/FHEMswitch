package de.fehngarten.fhemswitch;

//import android.util.Log; 

public class MySwitch
{
   public String name;
   public String unit;
   public String cmd;
   public String icon;

   public MySwitch(String name, String unit, String cmd)
   {
      this.name = name;
      this.unit = unit;
      this.cmd = cmd;
      icon = "off";
   }

   public void setIcon(String icon)
   {
      //Log.i("icon",icon);
      if (icon.equals("on") || icon.equals("off") || icon.equals("set_on") || icon.equals("set_off") || icon.equals("set_toggle"))
      {
         this.icon = icon;
      }
      else
      {
         this.icon = "undefined";
      }

   }

   public String activateCmd()
   {
      String cmd = "set " + this.unit + " " + this.cmd;
      return cmd;
   }

}
