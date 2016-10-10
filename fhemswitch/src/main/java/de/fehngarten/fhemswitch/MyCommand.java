package de.fehngarten.fhemswitch;

public class MyCommand
{
   public String name;
   public String command;
   public Boolean activ;

   public MyCommand(String name, String command, Boolean activ)
   {
      this.name = name;
      this.command = command;
      this.activ = activ;
   }
}