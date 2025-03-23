package services.dataBaseService;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Stateless
public class IdDAO {
    @PersistenceContext
    private EntityManager em;



}
