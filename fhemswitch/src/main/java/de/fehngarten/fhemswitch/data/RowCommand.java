package de.fehngarten.fhemswitch.data;

public class RowCommand
{
   public String name;
   public String command;
   public Boolean activ;

   public RowCommand(String name, String command)
   {
      this.name = name;
      this.command = command;
      this.activ = false;
   }
}