/*
 * Copyright (C) 2016-2017 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.plan.common.item.subquery;

import com.actiontech.dble.config.ErrorCode;
import com.actiontech.dble.plan.common.context.NameResolutionContext;
import com.actiontech.dble.plan.common.context.ReferContext;
import com.actiontech.dble.plan.common.exception.MySQLOutPutException;
import com.actiontech.dble.plan.common.field.Field;
import com.actiontech.dble.plan.common.item.Item;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;

import java.util.List;

public class ItemInSubQuery extends ItemMultiRowSubQuery {
    private boolean isNeg;
    protected Item leftOperand;

    public ItemInSubQuery(String currentDb, Item leftOperand, SQLSelectQuery query, boolean isNeg) {
        super(currentDb, query);
        this.leftOperand = leftOperand;
        this.isNeg = isNeg;
        if (this.planNode.getColumnsSelected().size() > 1) {
            throw new MySQLOutPutException(ErrorCode.ER_OPERAND_COLUMNS, "", "Operand should contain 1 column(s)");
        }
        this.select = this.planNode.getColumnsSelected().get(0);
    }

    public Item fixFields(NameResolutionContext context) {
        super.fixFields(context);
        leftOperand = leftOperand.fixFields(context);
        return this;
    }

    /**
     * added to construct all refers in an item
     *
     * @param context
     */
    public void fixRefer(ReferContext context) {
        super.fixRefer(context);
        leftOperand.fixRefer(context);
    }

    @Override
    public SubSelectType subType() {
        return SubSelectType.IN_SUBS;
    }

    @Override
    public SQLExpr toExpression() {
        SQLExpr expr = leftOperand.toExpression();
        SQLSelect sqlSelect = new SQLSelect(query);
        SQLInSubQueryExpr inSub = new SQLInSubQueryExpr(sqlSelect);
        inSub.setExpr(expr);
        inSub.setNot(isNeg);
        return inSub;
    }

    @Override
    protected Item cloneStruct(boolean forCalculate, List<Item> calArgs, boolean isPushDown, List<Field> fields) {
        return new ItemInSubQuery(this.currentDb, this.leftOperand.cloneItem(), this.query, this.isNeg);
    }

    public Item getLeftOperand() {
        return leftOperand;
    }

    public boolean isNeg() {
        return isNeg;
    }
}
