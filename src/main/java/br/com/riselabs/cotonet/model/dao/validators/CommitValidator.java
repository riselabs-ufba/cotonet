package br.com.riselabs.cotonet.model.dao.validators;

import br.com.riselabs.cotonet.model.beans.Commit;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;

public class CommitValidator implements Validator<Commit> {

	@Override
	public boolean validate(Commit c) throws InvalidCotonetBeanException {
		if (c.getHash() == null || c.getHash().length() != 40) {
			throw new InvalidCotonetBeanException(
					Commit.class,
					"Either the sha1 hash is inexistent, or its size is different than 40.",
					new NullPointerException());
		}
		if (c.getDatetime() == null) {
			throw new InvalidCotonetBeanException(Commit.class,
					"Commit date is inexistent.", new NullPointerException());
		}
		if (c.getCommitterID() == null || c.getAuthorID() == null) {
			throw new InvalidCotonetBeanException(
					Commit.class,
					"Either the committer ID, the author ID, or both are unavailable.",
					new NullPointerException());
		}
		return true;
	}

}
