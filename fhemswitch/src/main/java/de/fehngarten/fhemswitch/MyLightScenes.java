package de.fehngarten.fhemswitch;

import java.util.ArrayList;

public class MyLightScenes 
{
   public ArrayList<MyLightScene> lightScenes = null;
   public ArrayList<Item> items = new ArrayList<Item>();
   public int itemsCount = 0;

   public MyLightScenes()
   {
      lightScenes = new ArrayList<MyLightScene>();
      items = new ArrayList<Item>();
      itemsCount = 0;
   }

   public MyLightScene newLightScene(String name, String unit, Boolean showHeader)
   {
      MyLightScene myLightScene = new MyLightScene(name, unit, false, showHeader);
      lightScenes.add(myLightScene);
      return myLightScene;
   }

   public void aggregate()
   {
      items = new ArrayList<Item>();
      itemsCount = 0;
      for (MyLightScene lightScene : lightScenes)
      {
         if (lightScene.enabled)
         {
            String lightSceneUnit = lightScene.unit;
            if (lightScene.showHeader)
            {
               items.add(new Item(lightScene.unit, lightScene.name, lightScene.unit, true, false, lightScene.showHeader));
               itemsCount++;
            }
            for (MyLightScene.Member member : lightScene.members)
            {
               if (member.enabled)
               {
                  lightScene.enabled = true;
                  items.add(new Item(lightSceneUnit, member.name, member.unit, false, false, false));
                  itemsCount++;
               }
            }
         }
      }
   }

   public ArrayList<String> unitsList()
   {
      ArrayList<String> unitsList = new ArrayList<String>();
      for (MyLightScene myLightScene : lightScenes)
      {
         unitsList.add(myLightScene.unit);
      }
      return unitsList;   
   }
   
   class Item
   {
      String lightSceneName;
      String name;
      String unit;
      Boolean header;
      Boolean activ;
      Boolean showHeader;

      public Item(String lightSceneName, String name, String unit, Boolean header, Boolean activ, Boolean showHeader)
      {
         this.lightSceneName = lightSceneName;
         this.name = name;
         this.unit = unit;
         this.header = header;
         this.activ = activ;
         this.showHeader = showHeader;
      }
   }

   public String activateCmd(int pos)
   {
      String cmd = "set " + items.get(pos).lightSceneName + " scene " + items.get(pos).unit;
      return cmd;
   }

   public Boolean isLightScene(String unit)
   {
      for (MyLightScene lightScene : lightScenes)
      {
         if (lightScene.unit.equals(unit))
         {
            return true;
         }
      }
      return false;
   }
   
   class MyLightScene
   {
      public String name;
      public String unit;
      public Boolean enabled;
      public Boolean showHeader;

      public ArrayList<Member> members = new ArrayList<Member>();

      public MyLightScene(String name, String unit, Boolean enabled, Boolean showHeader)
      {
         this.name = name;
         this.unit = unit;
         this.enabled = enabled;
         this.showHeader = showHeader;
      }

      public void addMember(String name, String unit, Boolean enabled)
      {
         members.add(new Member(name, unit, enabled));
         if (enabled)
         {
            this.enabled = true;
         }
         aggregate();
      } 

      public Boolean isMember(String unit)
      {
         for (Member member : members)
         {
            if (member.unit.equals(unit))
            {
               return true;
            }
         }
         return false;
      }
      public void setActiv(String unit)
      {
         String lightSceneName = "";
         for (Item item : items)
         {
            if (item.unit.equals(unit))
            {
               lightSceneName = item.lightSceneName;
               break;
            }
         }
         for (Item item : items)
         {
            if (item.lightSceneName.equals(lightSceneName))
            {
               if (item.unit.equals(unit))
               {
                  item.activ = true;
               }
               else
               {
                  item.activ = false;
               }
            }
         }
      }

      class Member
      {
         public String name;
         public String unit;
         public Boolean enabled;
         
         public Member(String name, String unit, Boolean enabled)
         {
            this.name = name;
            this.unit = unit;
            this.enabled = enabled;
         }
      }
   }
}
