package de.fehngarten.fhemswitch.data;

public class MyCommand
{
   public String name;
   public String command;
   public Boolean activ;

   public MyCommand(String name, String command)
   {
      this.name = name;
      this.command = command;
      this.activ = false;
   }
}