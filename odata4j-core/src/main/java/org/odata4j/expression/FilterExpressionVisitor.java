package org.odata4j.expression;

import org.odata4j.internal.InternalUtil;
import org.odata4j.repack.org.apache.commons.codec.binary.Hex;

import java.util.ArrayDeque;
import java.util.Deque;

public class FilterExpressionVisitor extends PreOrderVisitor {

  private final StringBuilder sb = new StringBuilder();
  private final Deque<ExpressionFragment> stack = new ArrayDeque<ExpressionFragment>();

  private void push(String fragment) {
    sb.append(fragment);
  }

  @Override
  public void beforeDescend() {
    ExpressionFragment fragment = stack.peek();

    if (fragment == null)
      return;

    push(fragment.before());
  }

  @Override
  public void betweenDescend() {
    ExpressionFragment fragment = stack.peek();

    if (fragment == null)
      return;

    push(fragment.between());
  }

  @Override
  public void afterDescend() {
    ExpressionFragment fragment = stack.poll();

    if (fragment == null)
      return;

    push(fragment.after());
  }

  @Override
  public String toString() {
    return sb.toString();
  }

  // literals

  @Override
  public void visit(NullLiteral expr) {
    push("null");
  }

  @Override
  public void visit(BooleanLiteral expr) {
    push(Boolean.toString(expr.getValue()));
  }

  @Override
  public void visit(GuidLiteral expr) {
    push("guid'" + expr.getValue() + "'");
  }

  @Override
  public void visit(StringLiteral expr) {
    push("'" + expr.getValue().replace("'", "''") + "'");
  }

  @Override
  public void visit(Int64Literal expr) {
    push(expr.getValue() + "L");
  }

  @Override
  public void visit(IntegralLiteral expr) {
    push(Integer.toString(expr.getValue()));
  }

  @Override
  public void visit(DoubleLiteral expr) {
    push(Double.toString(expr.getValue()) + "d");
  }

  @Override
  public void visit(SingleLiteral expr) {
    push(expr.getValue() + "f");
  }

  @Override
  public void visit(DecimalLiteral expr) {
    push(expr.getValue() + "M");
  }

  @Override
  public void visit(BinaryLiteral expr) {
    push("binary'" + Hex.encodeHexString(expr.getValue()) + "'");
  }

  @Override
  public void visit(DateTimeLiteral expr) {
    push("datetime'" + InternalUtil.formatDateTimeForXml(expr.getValue()) + "'");
  }

  @Override
  public void visit(DateTimeOffsetLiteral expr) {
    push("datetimeoffset'" + InternalUtil.formatDateTimeOffsetForXml(expr.getValue()) + "'");
  }

  @Override
  public void visit(TimeLiteral expr) {
    push("time'" + InternalUtil.formatTimeForXml(expr.getValue()) + "'");
  }

  @Override
  public void visit(ByteLiteral expr) {
    push(Integer.toString(expr.getValue().intValue()));
  }

  @Override
  public void visit(SByteLiteral expr) {
    push(Byte.toString(expr.getValue()));
  }

  // non-literals

  @Override
  public void visit(String type) {
    push("'" + type + "'");
  }

  @Override
  public void visit(OrderByExpression.Direction direction) {
    push(direction == OrderByExpression.Direction.ASCENDING ? "asc" : "desc");
  }

  @Override
  public void visit(OrderByExpression expr) {
    stack.push(ORDERBY_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(AddExpression expr) {
    stack.push(ADD_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(AndExpression expr) {
    stack.push(AND_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(CastExpression expr) {
    stack.push(CAST_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(ConcatMethodCallExpression expr) {
    stack.push(CONCAT_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(DivExpression expr) {
    stack.push(DIV_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(EndsWithMethodCallExpression expr) {
    stack.push(ENDS_WITH_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(EntitySimpleProperty expr) {
    push(String.format("%s", expr.getPropertyName()));
  }

  @Override
  public void visit(EqExpression expr) {
    stack.push(EQ_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(GeExpression expr) {
    stack.push(GE_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(GtExpression expr) {
    stack.push(GT_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(IndexOfMethodCallExpression expr) {
    stack.push(INDEX_OF_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(IsofExpression expr) {
    stack.push(ISOF_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(LeExpression expr) {
    stack.push(LE_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(LengthMethodCallExpression expr) {
    stack.push(LENGTH_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(LtExpression expr) {
    stack.push(LT_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(ModExpression expr) {
    stack.push(MOD_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(MulExpression expr) {
    stack.push(MUL_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(NeExpression expr) {
    stack.push(NE_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(NegateExpression expr) {
    stack.push(NEGATE_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(NotExpression expr) {
    stack.push(NOT_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(OrExpression expr) {
    stack.push(OR_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(ParenExpression expr) {
    stack.push(PAREN_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(BoolParenExpression expr) {
    stack.push(BOOL_PAREN_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(ReplaceMethodCallExpression expr) {
    stack.push(REPLACE_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(StartsWithMethodCallExpression expr) {
    stack.push(STARTS_WITH_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(SubExpression expr) {
    stack.push(SUB_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(SubstringMethodCallExpression expr) {
    stack.push(SUBSTRING_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(SubstringOfMethodCallExpression expr) {
    stack.push(SUBSTRINGOF_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(ToLowerMethodCallExpression expr) {
    stack.push(TOLOWER_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(ToUpperMethodCallExpression expr) {
    stack.push(TOUPPER_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(TrimMethodCallExpression expr) {
    stack.push(TRIM_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(YearMethodCallExpression expr) {
    stack.push(YEAR_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(MonthMethodCallExpression expr) {
    stack.push(MONTH_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(DayMethodCallExpression expr) {
    stack.push(DAY_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(HourMethodCallExpression expr) {
    stack.push(HOUR_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(MinuteMethodCallExpression expr) {
    stack.push(MINUTE_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(SecondMethodCallExpression expr) {
    stack.push(SECOND_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(RoundMethodCallExpression expr) {
    stack.push(ROUND_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(FloorMethodCallExpression expr) {
    stack.push(FLOOR_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(CeilingMethodCallExpression expr) {
    stack.push(CEILING_EXPRESSION_FRAGMENT);
  }

  @Override
  public void visit(AggregateAnyFunction expr) {
    if (null != expr.getVariable()) {
      stack.push(new ExpressionFragment(String.format("/any(%s:", expr.getVariable()), "", ")"));
    } else {
      push("/any()");
    }
  }

  @Override
  public void visit(AggregateAllFunction expr) {
    if (null != expr.getVariable()) {
      stack.push(new ExpressionFragment(String.format("/all(%s:", expr.getVariable()), "", ")"));
    }
    else {
      push("/all()");
    }
  }

  private static final class ExpressionFragment {
    private final String beforeFragment;
    private final String betweenFragment;
    private final String afterFragment;

    private ExpressionFragment(String beforeFragment, String afterFragment) {
      this.beforeFragment  = beforeFragment;
      this.betweenFragment = "";
      this.afterFragment   = afterFragment;
    }

    private ExpressionFragment(String beforeFragment, String betweenFragment, String afterFragment) {
      this.beforeFragment  = beforeFragment;
      this.betweenFragment = betweenFragment;
      this.afterFragment   = afterFragment;
    }

    public String before() {
      return beforeFragment;
    }

    public String between() {
      return betweenFragment;
    }

    public String after() {
      return afterFragment;
    }
  }

  private static final ExpressionFragment ADD_EXPRESSION_FRAGMENT = new ExpressionFragment("", " add ", "");
  private static final ExpressionFragment AND_EXPRESSION_FRAGMENT = new ExpressionFragment("", " and ", "");
  private static final ExpressionFragment CAST_EXPRESSION_FRAGMENT = new ExpressionFragment("cast(", ",", ")");
  private static final ExpressionFragment CONCAT_EXPRESSION_FRAGMENT = new ExpressionFragment("concat(", ",", ")");
  private static final ExpressionFragment DIV_EXPRESSION_FRAGMENT = new ExpressionFragment("", " div ", "");
  private static final ExpressionFragment ENDS_WITH_EXPRESSION_FRAGMENT = new ExpressionFragment("endswith(", ",", ")");
  private static final ExpressionFragment EQ_EXPRESSION_FRAGMENT = new ExpressionFragment("", " eq ", "");
  private static final ExpressionFragment GE_EXPRESSION_FRAGMENT = new ExpressionFragment("", " ge ", "");
  private static final ExpressionFragment GT_EXPRESSION_FRAGMENT = new ExpressionFragment("", " gt ", "");
  private static final ExpressionFragment INDEX_OF_EXPRESSION_FRAGMENT = new ExpressionFragment("indexof(", ",", ")");
  private static final ExpressionFragment ISOF_EXPRESSION_FRAGMENT = new ExpressionFragment("isof(", ",", ")");
  private static final ExpressionFragment LE_EXPRESSION_FRAGMENT = new ExpressionFragment("", " le ", "");
  private static final ExpressionFragment LENGTH_EXPRESSION_FRAGMENT = new ExpressionFragment("length(", ")");
  private static final ExpressionFragment LT_EXPRESSION_FRAGMENT = new ExpressionFragment("", " lt ", "");
  private static final ExpressionFragment MOD_EXPRESSION_FRAGMENT = new ExpressionFragment("", " mod ", "");
  private static final ExpressionFragment MUL_EXPRESSION_FRAGMENT = new ExpressionFragment("", " mul ", "");
  private static final ExpressionFragment NE_EXPRESSION_FRAGMENT = new ExpressionFragment("", " ne ", "");
  private static final ExpressionFragment NEGATE_EXPRESSION_FRAGMENT = new ExpressionFragment("-", "");
  private static final ExpressionFragment NOT_EXPRESSION_FRAGMENT = new ExpressionFragment("not ", "");
  private static final ExpressionFragment OR_EXPRESSION_FRAGMENT = new ExpressionFragment("", " or ", "");
  private static final ExpressionFragment PAREN_EXPRESSION_FRAGMENT = new ExpressionFragment("(", ")");
  private static final ExpressionFragment BOOL_PAREN_EXPRESSION_FRAGMENT = new ExpressionFragment("(", ")");
  private static final ExpressionFragment REPLACE_EXPRESSION_FRAGMENT = new ExpressionFragment("replace(", ",", ")");
  private static final ExpressionFragment STARTS_WITH_EXPRESSION_FRAGMENT = new ExpressionFragment("startswith(", ",", ")");
  private static final ExpressionFragment SUB_EXPRESSION_FRAGMENT = new ExpressionFragment("", " sub ", "");
  private static final ExpressionFragment SUBSTRING_EXPRESSION_FRAGMENT = new ExpressionFragment("substring(", ",", ")");
  private static final ExpressionFragment SUBSTRINGOF_EXPRESSION_FRAGMENT = new ExpressionFragment("substringof(", ",", ")");
  private static final ExpressionFragment TOLOWER_EXPRESSION_FRAGMENT = new ExpressionFragment("tolower(", ")");
  private static final ExpressionFragment TOUPPER_EXPRESSION_FRAGMENT = new ExpressionFragment("toupper(", ")");
  private static final ExpressionFragment TRIM_EXPRESSION_FRAGMENT = new ExpressionFragment("trim(", ")");
  private static final ExpressionFragment YEAR_EXPRESSION_FRAGMENT = new ExpressionFragment("year(", ")");
  private static final ExpressionFragment MONTH_EXPRESSION_FRAGMENT = new ExpressionFragment("month(", ")");
  private static final ExpressionFragment DAY_EXPRESSION_FRAGMENT = new ExpressionFragment("day(", ")");
  private static final ExpressionFragment HOUR_EXPRESSION_FRAGMENT = new ExpressionFragment("hour(", ")");
  private static final ExpressionFragment MINUTE_EXPRESSION_FRAGMENT = new ExpressionFragment("minute(", ")");
  private static final ExpressionFragment SECOND_EXPRESSION_FRAGMENT = new ExpressionFragment("second(", ")");
  private static final ExpressionFragment ROUND_EXPRESSION_FRAGMENT = new ExpressionFragment("round(", ")");
  private static final ExpressionFragment FLOOR_EXPRESSION_FRAGMENT = new ExpressionFragment("floor(", ")");
  private static final ExpressionFragment CEILING_EXPRESSION_FRAGMENT = new ExpressionFragment("ceiling(", ")");
  private static final ExpressionFragment ORDERBY_EXPRESSION_FRAGMENT = new ExpressionFragment("", " ", "");

}
