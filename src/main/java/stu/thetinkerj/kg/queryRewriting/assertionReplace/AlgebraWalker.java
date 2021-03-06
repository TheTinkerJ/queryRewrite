// package org.buaa.nlsde.jianglili.reasoningquery.assertionReplace;
package stu.thetinkerj.kg.queryRewriting.assertionReplace;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.OpVisitorByType;
import org.apache.jena.sparql.algebra.op.*;

import java.util.Iterator;

/**
 * Created by jianglili on 2016/3/25.
 */
public class AlgebraWalker extends OpVisitorByType {


    private final OpVisitor visitor;
    private final boolean topDown;

    private AlgebraWalker(OpVisitor visitor, boolean topDown) {
        this.visitor = visitor;
        this.topDown = topDown;
    }

    public static void walkBottomUp(OpVisitor visitor, Op op) {
        op.visit(new AlgebraWalker(visitor, false));
    }

    @Override
    public void visit(OpBGP opBGP)
    { visit0(opBGP) ; }

    @Override
    protected void visit0(Op0 op) {
        op.visit(visitor) ;
    }

    @Override
    protected void visit1(Op1 op) {
        if ( topDown ) op.visit(visitor);
        if ( op.getSubOp() != null ) op.getSubOp().visit(this);
        if ( !topDown ) op.visit(visitor);
    }
    @Override
    protected void visit2(Op2 op) {

        if (topDown) op.visit(visitor);
        if (op.getLeft() != null) op.getLeft().visit(this);
        if (op.getRight() != null) op.getRight().visit(this);
        if (!topDown) op.visit(visitor);
    }

    @Override
    protected void visitN(OpN op) {
        if ( topDown ) op.visit(visitor);
        for ( Iterator<Op> iter = op.iterator() ; iter.hasNext() ; )
        {
            Op sub = iter.next();
            sub.visit(this);
        }
        if ( !topDown ) op.visit(visitor);
    }

    @Override
    protected void visitExt(OpExt op) {
        op.visit(visitor) ;
    }

    @Override
    protected void visitFilter(OpFilter op) {
        visit1(op);
    }

    @Override
    protected void visitLeftJoin(OpLeftJoin op) {
        visit2(op);
    }
}
