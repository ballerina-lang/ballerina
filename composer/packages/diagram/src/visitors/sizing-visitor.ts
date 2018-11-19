import { Assignment, Block, Visitor } from "@ballerina/ast-model";
import * as _ from "lodash";
import { DiagramUtils } from "../diagram/diagram-utils";

// Following element is created to calculate the width of a text rendered in an svg.
// Please see getTextWidth on how we do the calculation.
const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
svg.setAttribute("style", "border: 0px");
svg.setAttribute("width", "600");
svg.setAttribute("height", "50");
svg.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink");
const textElement = document.createElementNS("http://www.w3.org/2000/svg", "text");
svg.appendChild(textElement);
document.body.appendChild(svg);

const config: any = DiagramUtils.getConfig();

/**
 * Get width of a given text and processed text
 * considering provided min and max widths.
 * @param {string} text
 * @param {number} minWidth
 * @param {number} maxWidth
 * @return {object} {width,text}
 */
function getTextWidth(text: string, minWidth = config.statement.width , maxWidth = config.statement.maxWidth) {
    textElement.innerHTML = _.escape(text);
    let width = config.statement.padding.left +
        textElement.getComputedTextLength() + config.statement.padding.right;
    // if the width is more then max width crop the text
    if (width <= minWidth) {
        // set the width to minimam width
        width = minWidth;
    } else if (width > minWidth && width <= maxWidth) {
        // do nothing
    } else {
        // We need to truncate displayText and show an ellipses at the end.
        const ellipses = "...";
        let possibleCharactersCount = 0;
        for (let i = (text.length - 1); i > 1; i--) {
            if ((config.statement.padding.left + textElement.getSubStringLength(0, i) +
                config.statement.padding.right) < maxWidth) {
                possibleCharactersCount = i;
                break;
            }
        }
        // We need room for the ellipses as well, hence removing 'ellipses.length' no. of characters.
        text = text.substring(0, (possibleCharactersCount - ellipses.length)) + ellipses; // Appending ellipses.

        width = maxWidth;
    }
    return {
        text,
        w: width,
    };
}

export const visitor: Visitor = {

    beginVisitAssignment(node: Assignment) {
        node.viewState = {};
    },

    endVisitBlock(node: Block) {
        let height: number = 0;
        node.statements.forEach((statement) => {
            if (statement.viewState) {
                height += statement.viewState.height;
            }
        });
        getTextWidth("sample");
        node.viewState = {
            height,
        };
    },
};
