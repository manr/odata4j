package org.odata4j.test.unit.expressions;


import junit.framework.Assert;
import org.core4j.Enumerable;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;
import org.odata4j.core.Guid;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.expression.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class FilterExpressionVisitorTest {

  @Test
  public void testExpressionVisitor() {

    ExpressionParser.DUMP_EXPRESSION_INFO = true;

    t(Expression.null_(), "null");
    t(Expression.boolean_(true), "true");
    t(Expression.boolean_(false), "false");

    t(Expression.string(""), "''");
    t(Expression.string("foo"), "'foo'");
    t(Expression.string(" foo "), "' foo '");
    t(Expression.string("fo'o"), "'fo''o'");

    t(Expression.integral(0), "0");
    t(Expression.integral(2), "2");
    t(Expression.integral(-2), "-2");
    t(Expression.integral(222222222), "222222222");
    t(Expression.integral(-222222222), "-222222222");
    t(Expression.int64(-2), "-2L");
    t(Expression.single(-2f), "-2.0f");
    t(Expression.single(-2.34f), "-2.34f");
    t(Expression.double_(-2.34d), "-2.34d");
    t(Expression.double_(-2E+1), "-20.0d");
    t(Expression.double_(2E-1), "0.2d");
    t(Expression.double_(-2.1E+1), "-21.0d");
    t(Expression.double_(-2.1E-1), "-0.21d");
    t(Expression.decimal(new BigDecimal("2")), "2M");
    t(Expression.decimal(new BigDecimal("2.34")), "2.34M");
    t(Expression.decimal(new BigDecimal("-2")), "-2M");
    t(Expression.decimal(new BigDecimal("-2.34")), "-2.34M");
    t(Expression.dateTime(new LocalDateTime("2008-10-13")), "datetime'2008-10-13T00:00:00'");

    // new DateTime(<string>) does *not* preserve the timezone!
    DateTime dto = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ").withOffsetParsed().parseDateTime("2008-10-13T00:00:00-04:00");
    t(Expression.dateTimeOffset(dto), "datetimeoffset'2008-10-13T00:00:00-04:00'");
    t(Expression.time(new LocalTime("13:20:00")), "time'PT13H20M'");
    t(Expression.guid(Guid.fromString("12345678-aaaa-bbbb-cccc-ddddffffffff")), "guid'12345678-aaaa-bbbb-cccc-ddddffffffff'");
    t(Expression.guid(Guid.fromString("bf4eeb4d-2ded-4aa6-a167-0571e1057e3b")), "guid'bf4eeb4d-2ded-4aa6-a167-0571e1057e3b'");

    t(Expression.decimal(new BigDecimal("2.345")), "2.345M");
    t(Expression.binary(new byte[]{(byte) 0xff}), "binary'ff'");
    t(Expression.binary(new byte[]{(byte) 0x00, (byte) 0xaa, (byte) 0xff}), "binary'00aaff'");

    t(Expression.simpleProperty("LastName"), "LastName");

    t(Expression.eq(Expression.simpleProperty("LastName"), Expression.string("foo")), "LastName eq 'foo'");
    t(Expression.eq(Expression.string("foo"), Expression.simpleProperty("LastName")), "'foo' eq LastName");

    t(Expression.ne(Expression.simpleProperty("LastName"), Expression.string("foo")), "LastName ne 'foo'");

    EqExpression exp = Expression.eq(Expression.simpleProperty("a"), Expression.integral(1));
    t(Expression.and(exp, exp), "a eq 1 and a eq 1");
    t(Expression.or(exp, exp), "a eq 1 or a eq 1");
    t(Expression.or(exp, Expression.and(exp, exp)), "a eq 1 or a eq 1 and a eq 1");
    t(Expression.or(Expression.and(exp, exp), exp), "a eq 1 and a eq 1 or a eq 1");
    t(Expression.and(Expression.boolean_(true), Expression.boolean_(false)), "true and false");

    t(Expression.lt(Expression.simpleProperty("a"), Expression.integral(1)), "a lt 1");
    t(Expression.gt(Expression.simpleProperty("a"), Expression.integral(1)), "a gt 1");
    t(Expression.le(Expression.simpleProperty("a"), Expression.integral(1)), "a le 1");
    t(Expression.ge(Expression.simpleProperty("a"), Expression.integral(1)), "a ge 1");

    t(Expression.add(Expression.integral(1), Expression.integral(2)), "1 add 2");
    t(Expression.sub(Expression.integral(1), Expression.integral(2)), "1 sub 2");
    t(Expression.mul(Expression.integral(1), Expression.integral(2)), "1 mul 2");
    t(Expression.div(Expression.integral(1), Expression.integral(2)), "1 div 2");
    t(Expression.mod(Expression.integral(1), Expression.integral(2)), "1 mod 2");

    t(Expression.paren(Expression.null_()), "(null)");
    t(Expression.paren(Expression.paren(Expression.null_())), "((null))");
    t(Expression.add(Expression.paren(Expression.integral(1)), Expression.paren(Expression.integral(2))), "(1) add (2)");

    t(Expression.not(Expression.null_()), "not null");
    t(Expression.negate(Expression.simpleProperty("a")), "-a");
    t(Expression.cast(EdmSimpleType.STRING.getFullyQualifiedTypeName()), "cast('Edm.String')");
    t(Expression.cast(Expression.null_(), EdmSimpleType.STRING.getFullyQualifiedTypeName()), "cast(null,'Edm.String')");
    t(Expression.isof(EdmSimpleType.STRING.getFullyQualifiedTypeName()), "isof('Edm.String')");

    t(Expression.endsWith(Expression.string("aba"), Expression.string("a")), "endswith('aba','a')");
    t(Expression.startsWith(Expression.string("aba"), Expression.string("a")), "startswith('aba','a')");
    t(Expression.substringOf(Expression.string("aba"), Expression.string("a")), "substringof('aba','a')");
    t(Expression.substringOf(Expression.string("aba")), "substringof('aba')");
    t(Expression.indexOf(Expression.string("aba"), Expression.string("a")), "indexof('aba','a')");
    t(Expression.replace(Expression.string("aba"), Expression.string("a"), Expression.string("b")), "replace('aba','a','b')");
    t(Expression.toLower(Expression.string("aba")), "tolower('aba')");
    t(Expression.toUpper(Expression.string("aba")), "toupper('aba')");
    t(Expression.trim(Expression.string("aba")), "trim('aba')");
    t(Expression.substring(Expression.string("aba"), Expression.integral(1)), "substring('aba',1)");
    t(Expression.substring(Expression.string("aba"), Expression.integral(1), Expression.integral(2)), "substring('aba',1,2)");
    t(Expression.concat(Expression.string("a"), Expression.string("b")), "concat('a','b')");
    t(Expression.length(Expression.string("aba")), "length('aba')");

    t(Expression.substringOf(Expression.simpleProperty("Name"), Expression.string("Boris")), "substringof(Name,'Boris')");

    t(Expression.year(Expression.string("aba")), "year('aba')");
    t(Expression.month(Expression.string("aba")), "month('aba')");
    t(Expression.day(Expression.string("aba")), "day('aba')");
    t(Expression.hour(Expression.string("aba")), "hour('aba')");
    t(Expression.minute(Expression.string("aba")), "minute('aba')");
    t(Expression.second(Expression.string("aba")), "second('aba')");
    t(Expression.round(Expression.string("aba")), "round('aba')");
    t(Expression.ceiling(Expression.string("aba")), "ceiling('aba')");
    t(Expression.floor(Expression.string("aba")), "floor('aba')");

    o("a desc", Expression.orderBy(Expression.simpleProperty("a"), OrderByExpression.Direction.DESCENDING));
    o("a asc", Expression.orderBy(Expression.simpleProperty("a"), OrderByExpression.Direction.ASCENDING));
    o("b desc, a asc", Expression.orderBy(Expression.simpleProperty("b"), OrderByExpression.Direction.DESCENDING), Expression.orderBy(Expression.simpleProperty("a"), OrderByExpression.Direction.ASCENDING));
  }

  private void o(String expected, OrderByExpression... expressions) {
    List<String> values = new ArrayList<String>();

    for (OrderByExpression expr : expressions) {
      FilterExpressionVisitor visitor = new FilterExpressionVisitor();
      visitor.visitNode(expr);
      values.add(visitor.toString());
    }

    String actual = Enumerable.create(values).join(", ");
    Assert.assertEquals(expected, actual);
  }

  private void t(CommonExpression expression, String expected) {
    FilterExpressionVisitor visitor = new FilterExpressionVisitor();
    visitor.visitNode(expression);

    Assert.assertEquals(expected, visitor.toString());
  }

  @Test
  public void testAny() {
    t(Expression.any(Expression.simpleProperty("Actors")), "Actors/any()");
  }

  @Test
  public void testAnyPredicate() {
    t(Expression.any(
        Expression.simpleProperty("Actors"),
        "a",
        Expression.eq(
            Expression.simpleProperty("a/FirstName"),
            Expression.string("Charlize"))),
        "Actors/any(a:a/FirstName eq 'Charlize')");
  }

  @Test
  public void testAnyNestedPredicate() {
    t(Expression.any(
        Expression.simpleProperty("Actors"),
        "a",
        Expression.any(
            Expression.simpleProperty("a/Awards"),
            "w",
            Expression.eq(
                Expression.simpleProperty("w/Name"),
                Expression.string("Oscar")))),
        "Actors/any(a:a/Awards/any(w:w/Name eq 'Oscar'))");
  }

  @Test
  public void testAllPredicate() {
    // it could happen...
    t(Expression.all(
        Expression.simpleProperty("Actors"),
        "a",
        Expression.eq(
            Expression.simpleProperty("a/FirstName"),
            Expression.string("Charlize"))),
        "Actors/all(a:a/FirstName eq 'Charlize')");
  }

  @Test
  public void testAllNestedPredicate() {
    // now that is a cast..
    t(Expression.all(
        Expression.simpleProperty("Actors"),
        "a",
        Expression.all(
            Expression.simpleProperty("a/Awards"),
            "w",
            Expression.eq(
                Expression.simpleProperty("w/Name"),
                Expression.string("Oscar")))),
        "Actors/all(a:a/Awards/all(w:w/Name eq 'Oscar'))");
  }

  @Test
  public void testAnyAllNestedPredicate() {
    t(Expression.any(
        Expression.simpleProperty("Actors"),
        "a",
        Expression.all(
            Expression.simpleProperty("a/Awards"),
            "w",
            Expression.eq(
                Expression.simpleProperty("w/Name"),
                Expression.string("Oscar")))),
        "Actors/any(a:a/Awards/all(w:w/Name eq 'Oscar'))");
  }

  @Test
  public void testAnyAllNestedPredicate2() {
    t(Expression.any(
        Expression.simpleProperty("Actors"),
        "a",
        Expression.or(
            Expression.all(
                Expression.simpleProperty("a/Awards"),
                "w",
                Expression.eq(
                    Expression.simpleProperty("w/Name"),
                    Expression.string("Oscar"))),
            Expression.any(
                Expression.simpleProperty("a/Houses"),
                "h",
                Expression.eq(
                    Expression.simpleProperty("h/City"),
                    Expression.string("Malibu"))))),
        "Actors/any(a:a/Awards/all(w:w/Name eq 'Oscar') or a/Houses/any(h:h/City eq 'Malibu'))");
  }

  @Test
  public void testAnyPredicateOnCollectionProperty() {
    t(Expression.any(
        Expression.simpleProperty("Tags"),
        "t",
        Expression.eq(
            Expression.simpleProperty("t"),
            Expression.string("Beautiful"))),
        "Tags/any(t:t eq 'Beautiful')");
  }

}
