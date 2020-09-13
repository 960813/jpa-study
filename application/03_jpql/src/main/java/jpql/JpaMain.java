package jpql;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            for (int i = 0; i < 5; ++i) {
                Team team = new Team();
                team.setName("team" + i);
                em.persist(team);

                Member member = new Member();
                member.setUsername("member" + i);
                member.setAge(i);
                member.setTeam(team);
                em.persist(member);
            }

            em.flush();
            em.clear();

            int resultCount = em.createQuery("update Member m set m.age = 20")
                    .executeUpdate();

            em.clear();

            System.out.println("resultCount = " + resultCount);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }
}
