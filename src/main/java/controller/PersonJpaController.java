/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import model.Doctor;
import model.Patient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import controller.exceptions.IllegalOrphanException;
import controller.exceptions.NonexistentEntityException;
import java.util.Collection;
import model.Person;

/**
 *
 * @author Adrien Foucart
 */
public class PersonJpaController implements Serializable {

    public PersonJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Person person) {
        if (person.getDoctorCollection() == null) {
            person.setDoctorCollection(new ArrayList<Doctor>());
        }
        if (person.getPatientCollection() == null) {
            person.setPatientCollection(new ArrayList<Patient>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Doctor> attachedDoctorCollection = new ArrayList<Doctor>();
            for (Doctor doctorCollectionDoctorToAttach : person.getDoctorCollection()) {
                doctorCollectionDoctorToAttach = em.getReference(doctorCollectionDoctorToAttach.getClass(), doctorCollectionDoctorToAttach.getIddoctor());
                attachedDoctorCollection.add(doctorCollectionDoctorToAttach);
            }
            person.setDoctorCollection(attachedDoctorCollection);
            List<Patient> attachedPatientList = new ArrayList<Patient>();
            for (Patient patientListPatientToAttach : person.getPatientCollection()) {
                patientListPatientToAttach = em.getReference(patientListPatientToAttach.getClass(), patientListPatientToAttach.getIdpatient());
                attachedPatientList.add(patientListPatientToAttach);
            }
            person.setPatientCollection(attachedPatientList);
            em.persist(person);
            for (Doctor doctorCollectionDoctor : person.getDoctorCollection()) {
                Person oldIdpersonOfDoctorListDoctor = doctorCollectionDoctor.getPerson();
                doctorCollectionDoctor.setPerson(person);
                doctorCollectionDoctor = em.merge(doctorCollectionDoctor);
                if (oldIdpersonOfDoctorListDoctor != null) {
                    oldIdpersonOfDoctorListDoctor.getDoctorCollection().remove(doctorCollectionDoctor);
                    oldIdpersonOfDoctorListDoctor = em.merge(oldIdpersonOfDoctorListDoctor);
                }
            }
            for (Patient patientListPatient : person.getPatientCollection()) {
                Person oldIdpersonOfPatientListPatient = patientListPatient.getIdperson();
                patientListPatient.setIdperson(person);
                patientListPatient = em.merge(patientListPatient);
                if (oldIdpersonOfPatientListPatient != null) {
                    oldIdpersonOfPatientListPatient.getPatientCollection().remove(patientListPatient);
                    oldIdpersonOfPatientListPatient = em.merge(oldIdpersonOfPatientListPatient);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Person person) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Person persistentPerson = em.find(Person.class, person.getId());
            Collection<Doctor> doctorCollectionOld = persistentPerson.getDoctorCollection();
            Collection<Doctor> doctorCollectionNew = person.getDoctorCollection();
            Collection<Patient> patientListOld = persistentPerson.getPatientCollection();
            Collection<Patient> patientListNew = (List<Patient>) person.getPatientCollection();
            List<String> illegalOrphanMessages = null;
            for (Doctor doctorListOldDoctor : doctorCollectionOld) {
                if (!doctorCollectionNew.contains(doctorListOldDoctor)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Doctor " + doctorListOldDoctor + " since its idperson field is not nullable.");
                }
            }
            for (Patient patientListOldPatient : patientListOld) {
                if (!patientListNew.contains(patientListOldPatient)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Patient " + patientListOldPatient + " since its idperson field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Doctor> attachedDoctorListNew = new ArrayList<Doctor>();
            for (Doctor doctorListNewDoctorToAttach : doctorCollectionNew) {
                doctorListNewDoctorToAttach = em.getReference(doctorListNewDoctorToAttach.getClass(), doctorListNewDoctorToAttach.getIddoctor());
                attachedDoctorListNew.add(doctorListNewDoctorToAttach);
            }
            doctorCollectionNew = attachedDoctorListNew;
            person.setDoctorCollection(doctorCollectionNew);
            List<Patient> attachedPatientListNew = new ArrayList<Patient>();
            for (Patient patientListNewPatientToAttach : patientListNew) {
                patientListNewPatientToAttach = em.getReference(patientListNewPatientToAttach.getClass(), patientListNewPatientToAttach.getIdpatient());
                attachedPatientListNew.add(patientListNewPatientToAttach);
            }
            patientListNew = attachedPatientListNew;
            person.setPatientCollection(patientListNew);
            person = em.merge(person);
            for (Doctor doctorListNewDoctor : doctorCollectionNew) {
                if (!doctorCollectionOld.contains(doctorListNewDoctor)) {
                    Person oldIdpersonOfDoctorListNewDoctor = doctorListNewDoctor.getPerson();
                    doctorListNewDoctor.setPerson(person);
                    doctorListNewDoctor = em.merge(doctorListNewDoctor);
                    if (oldIdpersonOfDoctorListNewDoctor != null && !oldIdpersonOfDoctorListNewDoctor.equals(person)) {
                        oldIdpersonOfDoctorListNewDoctor.getDoctorCollection().remove(doctorListNewDoctor);
                        oldIdpersonOfDoctorListNewDoctor = em.merge(oldIdpersonOfDoctorListNewDoctor);
                    }
                }
            }
            for (Patient patientListNewPatient : patientListNew) {
                if (!patientListOld.contains(patientListNewPatient)) {
                    Person oldIdpersonOfPatientListNewPatient = patientListNewPatient.getIdperson();
                    patientListNewPatient.setIdperson(person);
                    patientListNewPatient = em.merge(patientListNewPatient);
                    if (oldIdpersonOfPatientListNewPatient != null && !oldIdpersonOfPatientListNewPatient.equals(person)) {
                        oldIdpersonOfPatientListNewPatient.getPatientCollection().remove(patientListNewPatient);
                        oldIdpersonOfPatientListNewPatient = em.merge(oldIdpersonOfPatientListNewPatient);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = person.getId();
                if (findPerson(id) == null) {
                    throw new NonexistentEntityException("The person with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Person person;
            try {
                person = em.getReference(Person.class, id);
                person.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The person with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Doctor> doctorListOrphanCheck = person.getDoctorCollection();
            for (Doctor doctorListOrphanCheckDoctor : doctorListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Person (" + person + ") cannot be destroyed since the Doctor " + doctorListOrphanCheckDoctor + " in its doctorList field has a non-nullable idperson field.");
            }
            Collection<Patient> patientListOrphanCheck = person.getPatientCollection();
            for (Patient patientListOrphanCheckPatient : patientListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Person (" + person + ") cannot be destroyed since the Patient " + patientListOrphanCheckPatient + " in its patientList field has a non-nullable idperson field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(person);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Person> findPersonEntities() {
        return findPersonEntities(true, -1, -1);
    }

    public List<Person> findPersonEntities(int maxResults, int firstResult) {
        return findPersonEntities(false, maxResults, firstResult);
    }

    private List<Person> findPersonEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Person.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Person findPerson(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Person.class, id);
        } finally {
            em.close();
        }
    }

    public int getPersonCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Person> rt = cq.from(Person.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public Person findDuplicate(String firstName, String familyName, Date dateofbirth){
        EntityManager em = getEntityManager();
        List<Person> results = em.createNamedQuery("Person.findDuplicate")
                .setParameter("firstname", firstName)
                .setParameter("familyname", familyName)
                .setParameter("dateofbirth", dateofbirth)
                .getResultList();
        if( results.isEmpty() ){
            return null;
        }
        
        return results.get(0);
    }
    
}
