/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2016 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jasperreports.engine.json.expression.member.evaluation;

import net.sf.jasperreports.engine.json.JRJsonNode;
import net.sf.jasperreports.engine.json.JsonNodeContainer;
import net.sf.jasperreports.engine.json.expression.EvaluationContext;
import net.sf.jasperreports.engine.json.expression.member.ArraySliceExpression;
import net.sf.jasperreports.engine.json.expression.member.MemberExpression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Narcis Marcu (narcism@users.sourceforge.net)
 */
public class ArraySliceExpressionEvaluator extends AbstractMemberExpressionEvaluator {
    private static final Log log = LogFactory.getLog(ArraySliceExpressionEvaluator.class);

    private ArraySliceExpression expression;

    public ArraySliceExpressionEvaluator(EvaluationContext evaluationContext, ArraySliceExpression expression) {
        super(evaluationContext);
        this.expression = expression;
    }

    @Override
    public JsonNodeContainer evaluate(JsonNodeContainer contextNode) {
        if (log.isDebugEnabled()) {
            log.debug("---> evaluating expression [" + expression +
                    "] on a node with (size: " + contextNode.getSize() +
                    ", cSize: " + contextNode.getContainerSize() + ")");
        }

        JsonNodeContainer result = new JsonNodeContainer();

        switch(expression.getDirection()) {
            case DOWN:
            case ANYWHERE_DOWN:
                // this only make sense for containers with appropriate size
                if (contextNode.isContainer()) {
                    Integer start = getSliceStart(contextNode.getContainerSize());
                    if (start >= contextNode.getContainerSize()) {
                        return null;
                    }

                    Integer end = getSliceEnd(contextNode.getContainerSize());
                    if (end < 0) {
                        return null;
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("start: " + start + ", end: " + end);
                    }

                    // look for the nodes inside the container directly
                    if (contextNode.getSize() > 1) {
                        for (int i = start; i < end; i++) {
                            JRJsonNode nodeAtIndex = contextNode.getNodes().get(i);

                            if (applyFilter(nodeAtIndex)) {
                                result.add(nodeAtIndex);
                            }
                        }
                    }
                    // look for the nodes inside the first element which is supposed to be an ArrayNode
                    else {
                        JRJsonNode parentNode = contextNode.getFirst();
                        JsonNode arrayNode = parentNode.getDataNode();

                        for (int i = start; i < end; i++) {
                            JRJsonNode nodeAtIndex = parentNode.createChild(arrayNode.get(i));

                            if (applyFilter(nodeAtIndex)) {
                                result.add(nodeAtIndex);
                            }
                        }
                    }
                }
                break;
        }

        if (result.getSize() > 0) {
            return result;
        }

        return null;

    }

    private Integer getSliceStart(int containerSize) {
        Integer start = expression.getStart();

        if (start == null) {
            start = 0;
        } else if (start < 0) {
            start = containerSize + start;

            if (start < 0 ) {
                start = 0;
            }
        }

        return start;
    }

    private Integer getSliceEnd(int containerSize) {
        Integer end = expression.getEnd();

        if (end == null) {
            end = containerSize;
        } else if (end < 0) {
            end = containerSize + end;
        } else if (end > containerSize) {
            end = containerSize;
        }

        return end;
    }

    @Override
    public MemberExpression getMemberExpression() {
        return expression;
    }
}
