package de.fehngarten.fhemswitch.data;

import android.support.annotation.NonNull;

class MyCounter implements Comparable<MyCounter>
{
   public String type;
   int count;

   MyCounter(String type, int count)
   {
      this.type = type;
      this.count = count;
   }

   @Override
   public int compareTo(@NonNull MyCounter another)
   {
      return another.count - this.count;
   }
}