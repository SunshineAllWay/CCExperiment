package org.apache.batik.ext.awt.image.rendered;
import java.util.List;
import java.util.ArrayList;
public class TileBlock {
    int occX, occY, occW, occH;
    int xOff, yOff, w, h, benefit;
    boolean [] occupied;
    TileBlock(int occX, int occY, int occW, int occH, boolean [] occupied,
              int xOff, int yOff, int w, int h) {
        this.occX = occX;
        this.occY = occY;
        this.occW = occW;
        this.occH = occH;
        this.xOff = xOff;
        this.yOff = yOff;
        this.w    = w   ;
        this.h    = h   ;
        this.occupied = occupied;
        for (int y=0; y<h; y++)
            for (int x=0; x<w; x++)
                if (!occupied[x+xOff+occW*(y+yOff)])
                    benefit++;
    }
    public String toString() {
        String ret = "";
        for (int y=0; y<occH; y++) {
            for (int x=0; x<occW+1; x++) {
                if ((x==xOff) || (x==xOff+w)) {
                    if ((y==yOff) || (y==yOff+h-1))
                        ret += "+";
                    else  if ((y>yOff) && (y<yOff+h-1))
                        ret += "|";
                    else
                        ret += " ";
                }
                else if ((y==yOff)     && (x> xOff) && (x < xOff+w))
                    ret += "-";
                else if ((y==yOff+h-1) && (x> xOff) && (x < xOff+w))
                    ret += "_";
                else
                    ret += " ";
                if (x== occW)
                    continue;
                if (occupied[x+y*occW])
                    ret += "*";
                else
                    ret += ".";
            }
            ret += "\n";
        }
        return ret;
    }
    int getXLoc()    { return occX+xOff; }
    int getYLoc()    { return occY+yOff; }
    int getWidth()   { return w; }
    int getHeight()  { return h; }
    int getBenefit() { return benefit; }
    int getWork()    { return w*h+1; }
    static int getWork(TileBlock [] blocks) {
        int ret=0;
        for (int i=0; i<blocks.length; i++)
            ret += blocks[i].getWork();
        return ret;
    }
    TileBlock [] getBestSplit() {
        if (simplify())
            return null;
        if (benefit == w*h)
            return new TileBlock [] { this };
        return splitOneGo();
    }
    public TileBlock [] splitOneGo() {
        boolean [] filled = (boolean [])occupied.clone();
        List items = new ArrayList();
        for (int y=yOff; y<yOff+h; y++)
            for (int x=xOff; x<xOff+w; x++) {
                if (!filled[x+y*occW]) {
                    int cw = xOff+w-x;
                    for (int cx=x; cx<x+cw; cx++)
                        if (filled[cx+y*occW])
                            cw = cx-x;
                        else
                            filled[cx+y*occW] = true;  
                    int ch=1;
                    for (int cy=y+1; cy<yOff+h; cy++) {
                        int cx=x;
                        for (; cx<x+cw; cx++)
                            if (filled[cx+cy*occW])
                                break;
                        if (cx != x+cw)
                            break;
                        for (cx=x; cx<x+cw; cx++)
                            filled[cx+cy*occW] = true;
                        ch++;
                    }
                    items.add(new TileBlock(occX, occY, occW, occH,
                                            occupied, x, y, cw, ch));
                    x+=(cw-1);
                }
            }
        TileBlock [] ret = new TileBlock[items.size()];
        items.toArray( ret );
        return ret;
    }
    public boolean simplify() {
        boolean[] workOccupied = occupied;   
        for (int y=0; y<h; y++) {
            int x;
            for (x=0; x<w; x++)
                if (!workOccupied[x+xOff+occW*(y+yOff)])
                    break;
            if (x!=w) break;
            yOff++;
            y--;
            h--;
        }
        if (h==0) return true;
        for (int y=h-1; y>=0; y--) {
            int x;
            for (x=0; x<w; x++)
                if (!workOccupied[x+xOff+occW*(y+yOff)])
                    break;
            if (x!=w) break;
            h--;
        }
        for (int x=0; x<w; x++) {
            int y;
            for (y=0; y<h; y++)
                if (!workOccupied[x+xOff+occW*(y+yOff)])
                    break;
            if (y!=h) break;
            xOff++;
            x--;
            w--;
        }
        for (int x=w-1; x>=0; x--) {
            int y;
            for (y=0; y<h; y++)
                if (!workOccupied[x+xOff+occW*(y+yOff)])
                    break;
            if (y!=h) break;
            w--;
        }
        return false;
    }
}
