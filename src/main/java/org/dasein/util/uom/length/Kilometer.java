package org.dasein.util.uom.length;

import org.dasein.util.uom.time.TimePeriod;

import javax.annotation.Nonnull;
import java.text.ChoiceFormat;
import java.text.MessageFormat;

/**
 * [Class Documentation]
 * <p>Created by George Reese: 1/27/15 11:19</p>
 *
 * @author George Reese
 */
public class Kilometer extends LengthUnit {

    static public Length<Kilometer> valueOf(short km) {
        return new Length<Kilometer>(km, Length.KILOMETER);
    }

    static public Length<Kilometer> valueOf(int km) {
        return new Length<Kilometer>(km, Length.KILOMETER);
    }

    static public Length<Kilometer> valueOf(long km) {
        return new Length<Kilometer>(km, Length.KILOMETER);
    }

    static public Length<Kilometer> valueOf(double km) {
        return new Length<Kilometer>(km, Length.KILOMETER);
    }

    static public Length<Kilometer> valueOf(float km) {
        return new Length<Kilometer>(km, Length.KILOMETER);
    }

    static public Length<Kilometer> valueOf(Number km) {
        return new Length<Kilometer>(km, Length.KILOMETER);
    }

    public Kilometer() { }

    @Override
    public double getBaseUnitConversion() {
        return 1000.0;
    }

    @Override
    public @Nonnull String format(@Nonnull Number quantity) {
        MessageFormat fmt = new MessageFormat("{0}");

        fmt.setFormatByArgumentIndex(0, new ChoiceFormat(new double[] {0,1,2}, new String[] {"0 kilometers","1 kilometer","{0,number} kilometers"}));
        return fmt.format(new Object[] { quantity });
    }
}
