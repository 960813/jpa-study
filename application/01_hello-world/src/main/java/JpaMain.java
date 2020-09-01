import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            em.persist(member);

            // 순수 객체 상태를 고려해서 항상 양쪽에 값을 설정하자
            // Member->setTeam(), team.getMembers().add(member) 두 번을 호출하는게 번거롭다.
            // 연관관계 편의 메소드를 생성하는걸 권장.
//            team.getMembers().add(member); // **

            // 연관관계 편의 메소드
            // Team, Member 둘 중 한 곳에만 놔두기.
            team.addMember(member);

            // 양방향 매핑시 무한 루프가 발생할 수 있다.
            // EX> toString(), lombok, JSON 생성 라이브러리 등

//            em.flush();
//            em.clear();

            Team findTeam = em.find(Team.class, team.getId()); // 1차 캐시
            List<Member> members = findTeam.getMembers();

            for (Member m : members) {
                System.out.println("m = " + m.getUsername());
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
