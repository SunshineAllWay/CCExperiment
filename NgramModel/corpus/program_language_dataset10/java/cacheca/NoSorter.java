package edu.ucdavis.cacheca;

import org.eclipse.jdt.ui.text.java.AbstractProposalSorter;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * No sort
 */
public class NoSorter extends AbstractProposalSorter{

	@Override
	public int compare(ICompletionProposal p1, ICompletionProposal p2) {
		return -1;
	}

}
