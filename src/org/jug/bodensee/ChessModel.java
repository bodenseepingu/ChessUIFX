/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jug.bodensee;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 *
 * @author andi
 */
public class ChessModel {
    
    private final Map<Integer, StringProperty> PropMap = new HashMap <>();
    private final Map<Integer, Character> FigMap = new HashMap<>();
    private final Map<Character,String> FigString = new HashMap<>();
    private  int zugNr=1;
    /*
     *
     */
    public ChessModel() {
        
 
        for (int i=0; i< 64;i++)
            PropMap.put(i, new SimpleStringProperty());
        newGame();
    }

   
    
    StringProperty getProp(int row, int col) {
        return PropMap.get(8 * row + col);
    }

    //returns true on a valid move otherwise false
    boolean moveFigure(int rowS, int colS, int rowD, int colD, boolean isWhite) {
        //weisse Figuren kann man nicht ziehen
        char curFigure = FigMap.get(8 * rowS + colS);
        if (Character.isUpperCase(curFigure) && !isWhite) {
            System.out.println("Not possible to draw white figures");
            return false;
        }
        if (Character.isLowerCase(curFigure) && isWhite) {
            System.out.println("Not possible to draw black figures");
            return false;
        }
        if ("".equals(PropMap.get(8 * rowS + colS).getValue())) {
            System.out.println("No figure at location row " + rowS + " col: " + colS);
            return false;
        }
        //if (!"".equals(PropMap.get(8 * rowD + colD).getValue())) {
            //has to be changed - schlagen der gegnerischen Figur muss möglich sein
        //    System.out.println("Not free at location row " + rowS + " col: " + colS);
        //    return false;
        //}
        //change board via property map bindings
        PropMap.get(8 * rowD + colD).setValue(PropMap.get(8 * rowS + colS).getValue());
        PropMap.get(8 * rowS + colS).setValue("");
        //change model
        FigMap.put(8 * rowD + colD, FigMap.get(8 * rowS + colS));
        FigMap.put(8 * rowS + colS,' ');
        if (rowD == 7 && 'P' == FigMap.get(8 * rowD + colD)) {
            PropMap.get(8 * rowD + colD).setValue(FigString.get('Q'));
            FigMap.put(8 * rowD + colD,'Q');
        }
        if (rowD == 0 && 'p'== FigMap.get(8 * rowD + colD)) {
            PropMap.get(8 * rowD + colD).setValue(FigString.get('q'));
            FigMap.put(8 * rowD + colD,'q');
        }
        return true;
    }

    
    
    boolean moveFigure(String lastClickedFigure, String lastClickedField, boolean isWhite) {
        int rowS, colS, rowD, colD;
        
        rowS = Integer.parseInt(Character.toString(lastClickedFigure.charAt(0)));
        colS = Integer.parseInt(Character.toString(lastClickedFigure.charAt(1)));
        rowD = Integer.parseInt(Character.toString(lastClickedField.charAt(0)));
        colD = Integer.parseInt(Character.toString(lastClickedField.charAt(1)));
        System.out.println("rowS " + rowS + " colS "+colS+ " rowD "+rowD+" colD " + colD);
        return moveFigure(rowS, colS, rowD, colD, isWhite);
    }
    
    String getFENString(String sField, String dField) {
        String fen="";
        for (int row = 7;row >=0 ;row--) {
            int emptyFields = 0;
            for (int col = 0; col < 8;col++) {
                if (FigMap.get(8 * row + col) == ' ') { //empty
                    emptyFields ++;
                }
                else {
                    if (emptyFields > 0) { //first print out empty fields
                        fen = fen + emptyFields;
                        emptyFields = 0;
                    }
                    fen = fen + FigMap.get(8 * row + col);
                }
            }
            if (emptyFields > 0) { //might be there are some empty fields
                        fen = fen + emptyFields;
            }
            if (row > 0) { //delimiter Slash
                fen = fen + "/";
            }
        }
        
        System.out.println("Fenstring: <"+fen+">");
        return fen;
        
    }
    
    int getMoveNr(String bw) {
        // increment after black
        int zug = zugNr;
        if (bw.equals("b")) {
            zugNr++;
        }
        return zug;
    }
    
    String getCastlingPossibilities() {
        //currently not implemented
        return "-"; /* might be KQkq */
    }
    
    String getEnpassant() {
        //currently not implemented
        return "-";
    }
    
    int getHalfeMoveNr() {
        // currently not implemented
        return 0;
    }
                
    
    //returns Board Field from fieldID
    String getBoardField(String fieldID) {
        int row, col;
        row = Integer.parseInt(Character.toString(fieldID.charAt(0)));
        col = Integer.parseInt(Character.toString(fieldID.charAt(1)));
        
        String Pos = Character.toString((char) ('a'+col)) + Character.toString((char) ('1'+row));
        return Pos;
    }
    
    String getFieldID(char letter, char number) {
        int row = (int)(number - '1');
        int col = (int)(letter - 'a');
        String ID = Integer.toString(row) + Integer.toString(col);
        return ID;
    }

    void newGame() {
        zugNr = 1;
        FigMap.put(56,'r');
        FigMap.put(57,'n');
        FigMap.put(58,'b');
        FigMap.put(59,'q');
        FigMap.put(60,'k');
        FigMap.put(61,'b');
        FigMap.put(62,'n');
        FigMap.put(63,'r');
        FigMap.put(48,'p');
        FigMap.put(49,'p');
        FigMap.put(50,'p');
        FigMap.put(51,'p');
        FigMap.put(52,'p');
        FigMap.put(53,'p');
        FigMap.put(54,'p');
        FigMap.put(55,'p');
        FigMap.put(0,'R');
        FigMap.put(1,'N');
        FigMap.put(2,'B');
        FigMap.put(3,'Q');
        FigMap.put(4,'K');
        FigMap.put(5,'B');
        FigMap.put(6,'N');
        FigMap.put(7,'R');
        FigMap.put(8,'P');
        FigMap.put(9,'P');
        FigMap.put(10,'P');
        FigMap.put(11,'P');
        FigMap.put(12,'P');
        FigMap.put(13,'P');
        FigMap.put(14,'P');
        FigMap.put(15,'P');
        IntStream.range(16,48).forEach(i -> FigMap.put(i,' '));
        FigString.put('r',"♜");
        FigString.put('n',"♞");
        FigString.put('b',"♝");
        FigString.put('q',"♛");
        FigString.put('k',"♚");
        FigString.put('p',"♟");
        FigString.put('R',"♖");
        FigString.put('N',"♘");
        FigString.put('B',"♗");
        FigString.put('Q',"♕");
        FigString.put('K',"♔");
        FigString.put('P',"♙");
        FigString.put(' ', ""); //blank char for not occupied field
        for (int i=0; i< FigMap.size();i++) {
            
            PropMap.get(i).setValue(FigString.get(FigMap.get(i)));
        }
    }
}
