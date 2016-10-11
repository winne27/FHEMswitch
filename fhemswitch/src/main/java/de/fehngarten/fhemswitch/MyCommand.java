package de.fehngarten.fhemswitch;

class MyCommand
{
   public String name;
   public String command;
   Boolean activ;

   MyCommand(String name, String command, Boolean activ)
   {
      this.name = name;
      this.command = command;
      this.activ = activ;
   }
}