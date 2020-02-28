package frc.util;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class BotMath{
    //------ Span methods -----------
    // span in between inLo & inHi to outLo & outHi
    // -- inLo must be lower than inHi
    // -- inDelta (inhi - inLo) MUST NOT BE 0. Returns outLo
    // -- outLo asso w/ inLo & outHi asso with inHi
    // -- clamp - limit between outLo & outHi
    // -- sqrt - apply sqrt root to input then span
    public static double Span(double inVal, double inLo, double inHi,
                                            double outLo, double outHi,
                                            boolean clamp, int app){
        
        if(inHi == inLo) return 0.0;    //Invalid values
        if(outHi == outLo) return outLo;

        double tmp = (inVal - inLo) / (inHi - inLo);    //Calc in ratio
        double outDelta = ( outHi - outLo );

        switch(app){
            case 0: //Linear (Do nothing)
            break;            
            case 1: //Square
                tmp = ( tmp < 0 ? -1.0 : 1.0 ) * Math.sqrt( Math.abs( tmp )) * outDelta;
            break;            
            case 2: //Square root
                tmp = ( tmp < 0 ? -1.0 : 1.0 ) * Math.sqrt( Math.abs( tmp )) * outDelta;
            break;            
            default:
            break;            
        }
        tmp = (tmp * outDelta) + outLo; //Ratio time out range + offset

        if( clamp ) tmp = Clamp(tmp, outLo, outHi);

        return tmp;
    }

    // inVal is limited between outLo & outHi
    public static double Clamp( double inVal, double val1, double val2){
        double tmp = Math.min(val1, val2);
        if( inVal < tmp ){
            return tmp;
        }

        tmp = Math.max(val1, val2);
        if( inVal > tmp ){
            return tmp;
        }

        return inVal;
    }

    // This takes a 2-Dimensional array, In and out values.
    // In values must be increasing in value.
    // The inVal is then matched to between In values and spaned to Out values.
    // double inOutAr[][]  = {{-1.0, -0.15, -0.1, 0.1, 0.15, 1.0},
    //                        {-.8, -0.6,  0.0, 0.0, 0.6, 0.8}};
    public static double SegLine(double inVal, double arInOut[][] ){
        int arLen = arInOut[0].length - 1;
        if(inVal < arInOut[0][0]) return arInOut[1][0];
        if(inVal > arInOut[0][arLen]) return arInOut[1][arLen];
        int x = 0;
        while( ++x < arLen && inVal >= arInOut[0][x] );
        return BotMath.Span(inVal, arInOut[0][x-1], arInOut[0][x],
                                   arInOut[1][x-1], arInOut[1][x], true, 0);
    }

    // Calculate the rotational response using simple proportional.
    // Hdg units in degrees.  Range -180 (CCW) to 180 (CW)
    public static double calcRotation(double hdgFB, double hdgSP, double hdgPB, double hdgDB,
                                      double outMn, double outMx){
        //
        double err = BotMath.normalizeTo180(hdgFB - hdgSP);   //error, -180 to 180
        return calcProp(err, hdgSP, hdgPB, hdgDB, outMn, outMx);
        /*
        if(Math.abs(tmpD) < hdgDB || hdgPB == 0.0) return  0.0; //In deadband or PB is 0

        tmpD /= hdgPB;  //else calc proportional, neg. else pos.
        tmpD = tmpD < 0 ?
        BotMath.Span(tmpD, -1.0, 0.0, -outMx, -outMn, true, false) : //Neg.
        BotMath.Span(tmpD, 0.0, 1.0, outMn, outMx, true, false);     //else Pos.
        return tmpD;
        */
    }

    // Calculate a simple proportional responce.
    public static double calcProp(double inFB, double inSP, double inPB, double inDB,
                                      double outMn, double outMx){
        //
        double err = inFB - inSP;   //error
        if(Math.abs(err) < inDB || inPB == 0.0) return  0.0; //In deadband or PB is 0

        err /= inPB;  //else calc proportional, neg. else pos.
        err = err < 0 ?
        BotMath.Span(err, -1.0, 0.0, -outMx, -outMn, true, 0) : //Neg.
        BotMath.Span(err, 0.0, 1.0, outMn, outMx, true, 0);     //else Pos.
        return err;
    }

    // Left this here.  Does the same thing as clamp.
    public static boolean Between(double key, double min, double max) {
        return (key >= min && key <= max);
    }
    
    //---- Convert an array of booleans to an integer ----
    // No more than 15 items.  [0] is lsb.
    public static int Bool2Int( boolean arInBool[] ){
        int tmp = 0;
        int sizeAr = Math.min(arInBool.length, 15);
        for(int i = 0; i < sizeAr; i++){
            tmp = ( tmp << 1 ) + ( arInBool[i] ? 1 : 0 );   // Selection Operator
        }
        return tmp;
    }

    // Convert DI to int
    public static int DI2Int( DigitalInput arDI[] ){
        boolean arBool[] = new boolean[arDI.length];
        for( int i = 0; i < arDI.length; i++){
            arBool[i] = arDI[i].get();
        }
        return Bool2Int( arBool );
    }

    // Convert DO to int
    public static int DO2Int( DigitalOutput arDO[] ){
        boolean arBool[] = new boolean[arDO.length];
        for( int i = 0; i < arDO.length; i++){
            arBool[i] = arDO[i].get();
        }
        return Bool2Int( arBool );
    }

    public static double normalizeTo180( double inAngle){
        double tmpD = inAngle % 360.0;
        if( tmpD < -180.0 ){
            tmpD += 360.0;
        }else if(tmpD > 180){
            tmpD -= 360;
        }
        return tmpD;
    }
}