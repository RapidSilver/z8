package org.zenframework.z8.server.types;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.sql.sql_decimal;

public final class decimal extends primary {
	private static final long serialVersionUID = 2752764281506888344L;

	static public integer RoundUp = new integer(BigDecimal.ROUND_UP);
	static public integer RoundDown = new integer(BigDecimal.ROUND_DOWN);
	static public integer RoundCeiling = new integer(BigDecimal.ROUND_CEILING);
	static public integer RoundFloor = new integer(BigDecimal.ROUND_FLOOR);
	static public integer RoundHalfUp = new integer(BigDecimal.ROUND_HALF_UP);
	static public integer RoundHalfDown = new integer(BigDecimal.ROUND_HALF_DOWN);
	static public integer RoundHalfEven = new integer(BigDecimal.ROUND_HALF_EVEN);
	static public integer RoundUnnecessary = new integer(BigDecimal.ROUND_UNNECESSARY);

	static public decimal Zero = new decimal();

	private BigDecimal value = new BigDecimal(0);

	private static int maxPrecision = 38;

	public decimal() {
	}

	public decimal(String string) {
		set(string);
	}

	public decimal(string string) {
		this(string.get());
	}

	public decimal(decimal decimal) {
		set(decimal);
	}

	public decimal(integer x) {
		set(x);
	}

	public decimal(double decimal) {
		set(BigDecimal.valueOf(decimal));
	}

	public decimal(BigDecimal decimal) {
		set(decimal);
	}

	public decimal(long x) {
		set(new BigDecimal(x));
	}

	@Override
	public String toString() {
		return value.toPlainString();
	}

	public BigDecimal get() {
		return value;
	}

	public double getDouble() {
		return value.doubleValue();
	}

	public void set(BigDecimal number) {
		value = new BigDecimal(number.unscaledValue(), number.scale());
		if(number.precision() > decimal.maxPrecision)
			value = value.setScale(number.scale() - (number.precision() - maxPrecision), RoundingMode.CEILING);
	}

	private void set(integer value) {
		set(new BigDecimal(value.get()));
	}

	private void set(decimal value) {
		set(value.get());
	}

	private void set(String strConst) {
		set(z8_parse(new string(strConst)));
	}

	@Override
	public FieldType type() {
		return FieldType.Decimal;
	}

	@Override
	public String toDbConstant(DatabaseVendor dbtype) {
		switch(dbtype) {
		case SqlServer: {
			int scale = (scale() == 0 ? 1 : scale());
			int precision = ((precision() + scale) > maxPrecision ? maxPrecision - scale : precision()) + scale;
			return "CONVERT([numeric](" + Integer.toString(precision) + "," + Integer.toString(scale) + "),(" + toString() + "),(0))";
		}
		default:
			return toString();
		}
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object d) {
		if(d instanceof decimal) {
			return value.compareTo(((decimal)d).value) == 0;
		}
		return false;
	}

	@Override
	public int compareTo(primary primary) {
		if(primary instanceof decimal) {
			decimal decimal = (decimal)primary;
			return value.compareTo(decimal.value);
		}

		if(primary instanceof integer) {
			integer integer = (integer)primary;
			return value.compareTo(integer.decimal().value);
		}

		return -1;
	}

	public int precision() {
		return value.precision();
	}

	public int scale() {
		return value.scale();
	}

	public sql_decimal sql_decimal() {
		return new sql_decimal(this);
	}

	public decimal operatorAdd() {
		return this;
	}

	public decimal operatorSub() {
		return new decimal(value.negate());
	}

	public decimal operatorAdd(integer x) {
		return new decimal(value.add(new BigDecimal(x.get())));
	}

	public decimal operatorSub(integer x) {
		return new decimal(value.subtract(new BigDecimal(x.get())));
	}

	public decimal operatorMul(integer x) {
		return new decimal(value.multiply(new BigDecimal(x.get())));
	}

	public decimal operatorMod(integer x) {
		try {
			return new decimal(value.divideAndRemainder(new BigDecimal(x.get()))[1]);
		} catch(ArithmeticException e) {
			throw new RuntimeException(e);
		}
	}

	public decimal operatorAdd(decimal x) {
		return new decimal(value.add(x.value));
	}

	public decimal operatorSub(decimal x) {
		return new decimal(value.subtract(x.value));
	}

	public decimal operatorMul(decimal x) {
		return new decimal(value.multiply(x.value));
	}

	public decimal operatorDiv(integer x) {
		return operatorDiv(new decimal(x));
	}

	public decimal operatorDiv(decimal x) {
		BigDecimal divisor = x.get();

		MathContext mc = new MathContext((int)Math.min(precision() + (long)Math.ceil(10.0 * divisor.precision() / 3.0), Integer.MAX_VALUE));

		try {
			return new decimal(value.divide(divisor, mc));
		} catch(ArithmeticException e) {
			throw new RuntimeException(e);
		}
	}

	public decimal operatorMod(decimal x) {
		try {
			return new decimal(value.divideAndRemainder(x.get())[1]);
		} catch(ArithmeticException e) {
			throw new RuntimeException(e);
		}
	}

	public bool operatorEqu(integer x) {
		return operatorEqu(new decimal(x));
	}

	public bool operatorNotEqu(integer x) {
		return operatorNotEqu(new decimal(x));
	}

	public bool operatorLess(integer x) {
		return operatorLess(new decimal(x));
	}

	public bool operatorMore(integer x) {
		return operatorMore(new decimal(x));
	}

	public bool operatorLessEqu(integer x) {
		return operatorLessEqu(new decimal(x));
	}

	public bool operatorMoreEqu(integer x) {
		return operatorMoreEqu(new decimal(x));
	}

	public bool operatorEqu(decimal x) {
		return new bool(value.compareTo(x.get()) == 0);
	}

	public bool operatorNotEqu(decimal x) {
		return new bool(value.compareTo(x.get()) != 0);
	}

	public bool operatorLess(decimal x) {
		return new bool(value.compareTo(x.get()) < 0);
	}

	public bool operatorMore(decimal x) {
		return new bool(value.compareTo(x.get()) > 0);
	}

	public bool operatorLessEqu(decimal x) {
		return new bool(value.compareTo(x.get()) <= 0);
	}

	public bool operatorMoreEqu(decimal x) {
		return new bool(value.compareTo(x.get()) >= 0);
	}

	public decimal z8_abs() {
		return new decimal(Math.abs(value.doubleValue()));
	}

	public integer z8_signum() {
		return new integer((long)Math.signum(value.doubleValue()));
	}

	public integer z8_ceil() {
		return new integer((long)Math.ceil(value.doubleValue()));
	}

	public integer z8_floor() {
		return new integer((long)Math.floor(value.doubleValue()));
	}

	public integer round() {
		return new integer(Math.round(value.doubleValue()));
	}

	public decimal round(int digits) {
		return round(digits, RoundHalfUp);
	}

	public decimal round(int digits, integer mode) {
		return new decimal(value.setScale(digits, RoundingMode.valueOf(mode.getInt())));
	}

	public decimal round(integer digits) {
		return round(digits.getInt());
	}

	public decimal round(integer digits, integer mode) {
		return round(digits.getInt(), mode);
	}

	public integer z8_round() {
		return round();
	}

	public decimal z8_round(integer digits) {
		return round(digits);
	}

	public decimal z8_round(integer digits, integer mode) {
		return round(digits, mode);
	}

	public decimal z8_sin() {
		try {
			return new decimal(Math.sin(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_cos() {
		try {
			return new decimal(Math.cos(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_tan() {
		try {
			return new decimal(Math.tan(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_asin() {
		try {
			return new decimal(Math.asin(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_acos() {
		try {
			return new decimal(Math.acos(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_atan() {
		try {
			return new decimal(Math.atan(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_sinh() {
		try {
			return new decimal(Math.sinh(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_cosh() {
		try {
			return new decimal(Math.cosh(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_tanh() {
		try {
			return new decimal(Math.tanh(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_toRadians() {
		try {
			return new decimal(Math.toRadians(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_toDegrees() {
		try {
			return new decimal(Math.toDegrees(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_exp() {
		try {
			return new decimal(Math.exp(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_ln() {
		try {
			return new decimal(Math.log(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_log10() {
		try {
			return new decimal(Math.log10(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_sqrt() {
		try {
			return new decimal(Math.sqrt(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_cbrt() {
		try {
			return new decimal(Math.cbrt(value.doubleValue()));
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_power(decimal power) {
		return new decimal(Math.pow(value.doubleValue(), power.get().doubleValue()));
	}

	public decimal z8_power(integer power) {
		return new decimal(Math.pow(value.doubleValue(), power.get()));
	}

	static public decimal z8_parse(string value) {
		try {
			String string = value.get();
			if(string.isEmpty())
				return decimal.Zero;
			return new decimal(new BigDecimal(string));
		} catch(NumberFormatException e) {
			throw new RuntimeException("Invalid value for decimal: '" + value.get() + "'");
		}
	}
}
