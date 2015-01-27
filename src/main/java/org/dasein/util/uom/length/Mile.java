package org.dasein.util.uom.length;

import javax.annotation.Nonnull;
import java.text.ChoiceFormat;
import java.text.MessageFormat;

/**
 * [Class Documentation]
 * <p>Created by George Reese: 1/27/15 11:19</p>
 *
 * @author George Reese
 */
public class Mile extends LengthUnit {

    static public Length<Mile> valueOf(short miles) {
        return new Length<Mile>(miles, Length.MILE);
    }

    static public Length<Mile> valueOf(int miles) {
        return new Length<Mile>(miles, Length.MILE);
    }

    static public Length<Mile> valueOf(long miles) {
        return new Length<Mile>(miles, Length.MILE);
    }

    static public Length<Mile> valueOf(double miles) {
        return new Length<Mile>(miles, Length.MILE);
    }

    static public Length<Mile> valueOf(float miles) {
        return new Length<Mile>(miles, Length.MILE);
    }

    static public Length<Mile> valueOf(Number miles) {
        return new Length<Mile>(miles, Length.MILE);
    }

    public Mile() { }

    @Override
    public double getBaseUnitConversion() {
        return 1609.3;
    }

    @Override
    public @Nonnull String format(@Nonnull Number quantity) {
        MessageFormat fmt = new MessageFormat("{0}");

        fmt.setFormatByArgumentIndex(0, new ChoiceFormat(new double[] {0,1,2}, new String[] {"0 miles","1 mile","{0,number} miles"}));
        return fmt.format(new Object[] { quantity });
    }
}
