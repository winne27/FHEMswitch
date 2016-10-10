package de.fehngarten.fhemswitch;

public class MyCounter implements Comparable<MyCounter>
{
   public String type;
   public int count;

   public MyCounter(String type, int count)
   {
      this.type = type;
      this.count = count;
   }

   @Override
   public int compareTo(MyCounter another)
   {
      return another.count - this.count;
   }
}