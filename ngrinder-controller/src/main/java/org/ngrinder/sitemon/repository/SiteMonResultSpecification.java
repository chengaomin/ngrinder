/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.sitemon.repository;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ngrinder.sitemon.model.SiteMonResult;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Gisoo Gwon
 */
public abstract class SiteMonResultSpecification {

	/**
	 * Find all {@link SiteMonResult}s where equal siteMonId AND greater than startTime ORDER BY timestamp.
	 * @param siteMonId
	 * @param startTime
	 * @return
	 */
	public static Specification<SiteMonResult> idEqualAndAfterTimeOrderByTime(
		final String siteMonId, final Date startTime) {
		return new Specification<SiteMonResult>() {
			@Override
			public Predicate toPredicate(Root<SiteMonResult> root, CriteriaQuery<?> query,
				CriteriaBuilder cb) {
				Path<Object> id = root.get("siteMonResultPK").get("siteMonId");
				Expression<Date> timestamp = root.get("siteMonResultPK").get("timestamp").as(
					Date.class);
				query.orderBy(cb.asc(timestamp));
				return cb.and(cb.equal(id, siteMonId), cb.greaterThan(timestamp, startTime));
			}
		};
	}

}
