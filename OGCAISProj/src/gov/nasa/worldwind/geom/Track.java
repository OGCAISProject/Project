/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.geom;

import java.util.ArrayList;

/**
 *
 * @author Jing
 */
public class Track {
     private ArrayList<Position> positions = new ArrayList<Position>();
     private int id;
    
     
     public Track(int voyageid)
     {
         this.id =  voyageid; 
     }
     
     public void addPosition(Position position)
     {
         this.positions.add(position);
     }
    
     public int getID()
     {
         return this.id;
     }
     
     public ArrayList<Position> getPositions()
     {
         return this.positions;
     }
}
