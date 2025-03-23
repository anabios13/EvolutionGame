package services.dataBaseService;

import game.entities.Game;
import game.entities.Users;

import javax.ejb.Stateless;
import javax.persistence.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

@Stateless
public class UsersDAO {

    @PersistenceContext
    private EntityManager em; //because of JTA resource-type

    public boolean isPasswordValid(String login, String password) throws InvalidKeySpecException, NoSuchAlgorithmException, PersistenceException {

        if (login == null || password == null) return false;

        try {
            TypedQuery<byte[]> storedPasswordQuery = em.createQuery("select c.password from Users c where c.login=?1", byte[].class);
            storedPasswordQuery.setParameter(1, login);
            byte[] storedPassword = storedPasswordQuery.getSingleResult();

            TypedQuery<byte[]> saltQuery = em.createQuery("select c.salt from Users c where c.login=?1", byte[].class);
            saltQuery.setParameter(1, login);
            byte[] salt = saltQuery.getSingleResult();

            return PasswordEncryptionService.authenticate(password, storedPassword, salt);
        } catch (NoResultException e) {
            return false;
        }
    }

    public boolean addUser(String login, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, PersistenceException {
        if (!isLoginFree(login)) return false;
        byte[] salt = PasswordEncryptionService.generateSalt();
        Users user = new Users(login, PasswordEncryptionService.getEncryptedPassword(password, salt), salt);
        try {
            em.persist(user);
        } catch (EntityExistsException e) {
            return false;
        }
        return true;
    }

    public Users get(String login) throws PersistenceException {
        TypedQuery<Users> tq = em.createQuery("SELECT c FROM Users c where c.login=?1", Users.class);
        tq.setParameter(1, login);
        try{
            return tq.getSingleResult();
        } catch (NoResultException e){
            return null;
        }
    }

    public List<Game> getSavedGames(String login) throws PersistenceException {
        TypedQuery<Game> tq = em.createQuery("select c from Game c join c.players p where p.name=?1", Game.class);
        tq.setParameter(1, login);
        return tq.getResultList();
    }

    private boolean isLoginFree(String login) {
        TypedQuery<Long> tq = em.createQuery("SELECT c.id FROM Users c where c.login=?1", Long.class);
        tq.setParameter(1, login);
        List<Long> sameLogin = tq.getResultList();
        return sameLogin.size() == 0;
    }
}